package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_091_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("staff", "4_91");
					// Gandalf, Friend of the Shirefolk: strength +1 for each race spotted in the fellowship; no damage bonus
					put("gandalf", "1_72");
					// Ent Avenger: a [gandalf] companion who is no Gandalf; strength +2 only while 3 Ents can be spotted
					put("avenger", "6_27");
					// Gandalf's Staff (no subtitle): the same title; bearer must be Gandalf, a cost modifier on [gandalf]
					// spells and a skirmish ability, used only on purpose
					put("oldstaff", "2_22");
					put("sword", "1_299");      // Hobbit Sword: [shire] possession, cost 1, on a Hobbit; no other text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GandalfsStaffStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Gandalf's Staff, Walking Stick
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Artifact
		 * Subtype: Staff
		 * Vitality: +1
		 * Game Text: Bearer must be Gandalf.
		 * While you can spot 2 twilight tokens, Gandalf is <b>damage +2</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("staff");

		assertEquals("Gandalf's Staff", card.getBlueprint().getTitle());
		assertEquals("Walking Stick", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.STAFF));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getVitality());
	}

	@Test
	public void CannotBePlayedOnAnotherGandalfCompanionAndIsPlayedOnGandalfOnceHeIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var staff = scn.GetFreepsCard("staff");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveCompanionsToTable(avenger);
		scn.MoveCardsToHand(staff, gandalf);

		scn.StartGame();

		// Ent Avenger is a [gandalf] companion and Frodo a companion: neither is Gandalf.
		assertFalse(scn.FreepsPlayAvailable(staff));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPlayCard(gandalf);
		assertTrue(scn.FreepsPlayAvailable(staff));

		// Gandalf is the one bearer it may go on, so it goes on him unasked.
		scn.FreepsPlayCard(staff);
		assertAttachedTo(staff, gandalf);
		assertNotAttachedTo(staff, avenger);
		assertNotAttachedTo(staff, frodo);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void GandalfIsDamagePlusTwoOnceTwoTwilightTokensCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var staff = scn.GetFreepsCard("staff");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(gandalf, avenger);
		scn.AttachCardsTo(gandalf, staff);
		scn.MoveCardsToHand(sword);

		scn.StartGame();
		scn.SetTwilight(1);
		// A burden beside the one twilight token: a token of another kind, which does not count.
		scn.AddBurdens(1);

		assertAttachedTo(staff, gandalf);
		assertEquals(1, scn.GetTwilight());
		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetKeywordCount(gandalf, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(avenger, Keyword.DAMAGE));

		// The Hobbit Sword's cost of 1 is added to the pool: 2 twilight tokens.
		scn.FreepsPlayCard(sword);
		assertAttachedTo(sword, frodo);
		assertEquals(2, scn.GetTwilight());
		assertEquals(2, scn.GetKeywordCount(gandalf, Keyword.DAMAGE));
		// Another companion, not the bearer.
		assertEquals(0, scn.GetKeywordCount(avenger, Keyword.DAMAGE));

		// More than 2 still meets "2".
		scn.SetTwilight(3);
		assertEquals(2, scn.GetKeywordCount(gandalf, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileGandalfsStaffWithNoSubtitleIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var staff = scn.GetFreepsCard("staff");
		var gandalf = scn.GetFreepsCard("gandalf");
		var oldstaff = scn.GetFreepsCard("oldstaff");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, oldstaff);
		scn.MoveCardsToHand(staff);

		scn.StartGame();

		// The two cards share the title Gandalf's Staff, so they represent the same thing (CR 4.2 "unique").
		assertAttachedTo(oldstaff, gandalf);
		assertFalse(scn.FreepsPlayAvailable(staff));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
