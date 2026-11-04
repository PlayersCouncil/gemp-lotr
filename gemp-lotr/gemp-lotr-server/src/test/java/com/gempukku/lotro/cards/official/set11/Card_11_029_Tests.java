package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_029_Tests
{

	// The game starts at Fangorn Glade, a forest and no dwelling, on a Shadows-block path; its one text acts on a
	// move to it.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ease", "11_29");

					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: a [gandalf] Wizard companion; a strength bonus only
					// Saruman, The White: a [gandalf] Wizard ally (a Hobbit-set card, the one Free Peoples Wizard who is no
					// companion beside Radagast 31_17); a Gandalf is defender +1 beside him, and his abilities are used
					// only on purpose
					put("saruman", "32_24");
					// Ent Avenger: a [gandalf] companion who is no Wizard; a strength bonus only beside 3 Ents
					put("avenger", "6_27");
				}},
				new HashMap<>() {{
					putAll(VirtualTableScenario.ShadowsSites);
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	// The game starts at Streets of Bree, a dwelling and nothing else; its one text bars transferring followers.
	protected VirtualTableScenario GetStreetsOfBreeScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ease", "11_29");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: a [gandalf] Wizard companion; a strength bonus only
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is no Wizard
				}},
				new HashMap<>() {{
					putAll(VirtualTableScenario.ShadowsSites);
					put("site1", "18_140");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	// The game starts at Valley of the Silverlode, a dwelling and a river; its one text acts on a move to it.
	protected VirtualTableScenario GetValleyScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ease", "11_29");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: a [gandalf] Wizard companion; a strength bonus only
				}},
				new HashMap<>() {{
					putAll(VirtualTableScenario.ShadowsSites);
					put("site1", "11_261");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	@Test
	public void EasetheBurdenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Ease the Burden
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: Exert a [gandalf] Wizard to remove a burden (or 2 burdens if the fellowship is at a dwelling site).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("ease");

		assertEquals("Ease the Burden", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AwayFromADwellingItExertsAGandalfWizardToRemoveOneBurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ease = scn.GetFreepsCard("ease");
		var gandalf = scn.GetFreepsCard("gandalf");
		var saruman = scn.GetFreepsCard("saruman");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveCompanionsToTable(gandalf, avenger);
		scn.MoveAlliesToTable(saruman);
		scn.MoveCardsToHand(ease);

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.AddBurdens(3);

		assertEquals("Fangorn Glade", scn.GetCurrentSite().getBlueprint().getTitle());
		assertInHand(ease);
		assertEquals(3, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertEquals(0, scn.GetWoundsOn(avenger));

		assertTrue(scn.FreepsPlayAvailable(ease));
		scn.FreepsPlayCard(ease);

		// A [gandalf] Wizard companion or ally; not a [gandalf] companion who is no Wizard.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, saruman));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(avenger));
		scn.FreepsChooseCard(saruman);

		assertEquals(1, scn.GetWoundsOn(saruman));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(avenger));
		// At a site that is no dwelling, one burden.
		assertEquals(2, scn.GetBurdens());
		assertInDiscard(ease);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtADwellingItRemovesTwoBurdens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetStreetsOfBreeScenario();

		var ease = scn.GetFreepsCard("ease");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveCompanionsToTable(gandalf, avenger);
		scn.MoveCardsToHand(ease);

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.AddBurdens(3);

		assertEquals("Streets of Bree", scn.GetCurrentSite().getBlueprint().getTitle());
		assertInHand(ease);
		assertEquals(3, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(avenger));

		scn.FreepsPlayCard(ease);

		// The one [gandalf] Wizard exerts unasked.
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(avenger));
		assertEquals(1, scn.GetBurdens());
		assertInDiscard(ease);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtADwellingThatIsAlsoARiverItRemovesTwoBurdens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetValleyScenario();

		var ease = scn.GetFreepsCard("ease");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(ease);

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.AddBurdens(3);

		assertEquals("Valley of the Silverlode", scn.GetCurrentSite().getBlueprint().getTitle());
		assertInHand(ease);
		assertEquals(3, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(gandalf));

		scn.FreepsPlayCard(ease);

		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetBurdens());
		assertInDiscard(ease);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileTheOnlyGandalfWizardIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ease = scn.GetFreepsCard("ease");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveCompanionsToTable(gandalf, avenger);
		scn.MoveCardsToHand(ease);

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.AddBurdens(3);
		// Vitality 4 with 3 wounds: exhausted.  Ent Avenger, a [gandalf] companion who could exert, stands beside him.
		scn.AddWoundsToChar(gandalf, 3);
		scn.FreepsRefreshDecision();

		assertInHand(ease);
		assertEquals(3, scn.GetBurdens());
		assertEquals(3, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(avenger));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(ease));
	}

	@Test
	public void WithNoBurdenItIsPlayedAndTheExertionIsPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetStreetsOfBreeScenario();

		var ease = scn.GetFreepsCard("ease");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(ease);

		scn.StartGame(scn.GetFreepsSite("site1"));

		assertInHand(ease);
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(gandalf));

		// An event is played whatever its effect can do.
		assertTrue(scn.FreepsPlayAvailable(ease));
		scn.FreepsPlayCard(ease);

		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetBurdens());
		assertInDiscard(ease);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
