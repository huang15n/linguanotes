package com.linguanotes.service;

import com.linguanotes.repository.LanguageRepository;
import com.linguanotes.repository.NoteRepository;
import com.linguanotes.repository.LessonRepository;
import com.linguanotes.repository.TagRepository;
import com.linguanotes.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NoteServiceTest {
    @Test
    void noteOwnedByAnotherAccountIsNotFound() {
        NoteRepository notes = mock(NoteRepository.class);
        LanguageRepository languages = mock(LanguageRepository.class);
        TagRepository tags = mock(TagRepository.class);
        UserAccountRepository accounts = mock(UserAccountRepository.class);
        when(notes.findByIdAndOwnerId(41L, 9L)).thenReturn(Optional.empty());
        NoteService service = new NoteService(notes, languages, tags, accounts, mock(LessonRepository.class));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.get(41L, 9L));

        assertEquals(404, error.getStatusCode().value());
        verify(notes).findByIdAndOwnerId(41L, 9L);
    }
}