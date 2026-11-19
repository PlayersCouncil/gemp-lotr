package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_148_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("despair", "7_148");

					// [raider] Men: an Easterling and a Southron
					put("axeman", "4_224");     // Easterling Axeman: strength +3 and fierce only at 2 burdens, and there are none
					put("soldier", "4_220");    // Desert Soldier: his trigger is for a skirmish he is in, and he is in none
					// Ulaire Attea, Khamul of the East: a [raider] Nazgûl, no Man; his texts need 3 and 5 burdens
					put("khamul", "103_61");
					// Dunlending Brigand: a [dunland] Man with no text
					put("brigand", "4_10");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Glóin: a [dwarven] Dwarf; a skirmish ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: a Ring-bound [shire] Hobbit who is not the Ring-bearer
					// Elrond, Lord of Rivendell: an ally.  His start-of-turn heal finds nobody wounded, and his
					// fellowship ability is never used.
					put("elrond", "1_40");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FierceinDespairStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Fierce in Despair
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 7
		 * Type: Event
		 * Subtype: Regroup
		 * Game Text: Spot 2 [raider] Men to discard a companion (except the Ring-bearer).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("despair");

		assertEquals("Fierce in Despair", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.REGROUP));
		assertEquals(7, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithTwoRaiderMenItDiscardsACompanionWhoIsNotTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveCompanionsToTable(aragorn, gimli, sam);
		scn.MoveAlliesToTable(elrond);

		var despair = scn.GetShadowCard("despair");
		var axeman = scn.GetShadowCard("axeman");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToHand(despair);
		assertInHand(despair);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.MoveMinionsToTable(axeman, soldier);

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertInHand(despair);
		assertTrue(scn.ShadowPlayAvailable(despair));
		scn.ShadowPlayCard(despair);

		// Any companion but the Ring-bearer; no ally, no minion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gimli, sam));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, elrond, axeman));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(aragorn);

		assertInDiscard(aragorn);
		assertInDiscard(despair);
		assertInZone(Zone.FREE_CHARACTERS, frodo, gimli, sam);
		assertInZone(Zone.SUPPORT, elrond);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ARaiderCardThatIsNoManAndAManOfAnotherCultureAreNotSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var despair = scn.GetShadowCard("despair");
		var axeman = scn.GetShadowCard("axeman");
		var khamul = scn.GetShadowCard("khamul");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCardsToHand(despair);
		assertInHand(despair);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.MoveMinionsToTable(axeman, khamul, brigand);

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		// One [raider] Man, beside a [raider] Nazgûl and a [dunland] Man.
		assertInHand(despair);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(despair));
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
	}

	@Test
	public void WithNoCompanionButTheRingBearerItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveAlliesToTable(elrond);

		var despair = scn.GetShadowCard("despair");
		var axeman = scn.GetShadowCard("axeman");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToHand(despair);
		assertInHand(despair);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.MoveMinionsToTable(axeman, soldier);

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertInHand(despair);
		assertTrue(scn.ShadowPlayAvailable(despair));
		scn.ShadowPlayCard(despair);

		assertInDiscard(despair);
		assertInZone(Zone.FREE_CHARACTERS, frodo);
		assertInZone(Zone.SUPPORT, elrond);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
