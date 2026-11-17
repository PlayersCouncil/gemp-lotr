package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_266_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("breached", "7_266");
					put("soldier", "1_271");        // Orc Soldier: [sauron] Orc; his skirmish ability is used only on purpose
					put("band", "1_270");           // Orc Scouting Band: [sauron] Orc of another title, a tracker
					put("guard", "10_81");          // Cirith Ungol Guard: a [sauron] Uruk-hai, damage +1 and no other text
					put("hound", "7_192");          // Morgul Hound: a [wraith] Orc; +4 and fierce only at 2 wounds on the Ring-bearer

					put("aragorn", "1_89");         // Aragorn, Ranger of the North: [gondor] Man companion; his maneuver ability is used only on purpose
					put("gimli", "1_13");           // Gimli, Son of Gloin: [dwarven] Dwarf companion
					put("boromir", "1_97");         // Boromir, Son of Denethor: [gondor] Man companion; his skirmish ability is used only on purpose
					put("arwen", "1_30");           // Arwen, Daughter of Elrond: [elven] Elf companion, a ranger and no archer
					put("elrond", "1_40");          // Elrond, Lord of Rivendell: an ally, no companion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BreachedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Breached
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Regroup
		 * Game Text: Remove 4 threats and spot 2 [sauron] Orcs to make the Free Peoples player spot a companion and place that companion in the dead pile.
		*/

		var scn = GetScenario();

		var breached = scn.GetFreepsCard("breached");

		assertEquals("Breached", breached.getBlueprint().getTitle());
		assertNull(breached.getBlueprint().getSubtitle());
		assertFalse(breached.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, breached.getBlueprint().getSide());
		assertEquals(Culture.SAURON, breached.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, breached.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(breached, Timeword.REGROUP));
		assertEquals(1, breached.getBlueprint().getTwilightCost());
	}

	@Test
	public void RemovesFourThreatsAndTheFreePeoplesPlayerPlacesACompanionOfHisChoiceInTheDeadPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var breached = scn.GetShadowCard("breached");
		var soldier = scn.GetShadowCard("soldier");
		var band = scn.GetShadowCard("band");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		var arwen = scn.GetFreepsCard("arwen");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveCompanionsToTable(aragorn, gimli, boromir, arwen);
		scn.MoveAlliesToTable(elrond);
		scn.MoveCardsToHand(breached);
		assertInHand(breached);

		scn.StartGame();
		scn.SetTwilight(5);
		// Five companions: five threats, one more than the cost removes.
		scn.AddThreats(5);
		scn.MoveMinionsToTable(soldier, band);
		scn.FreepsRefreshDecision();
		assertEquals(5, scn.GetThreats());

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		assertEquals(5, scn.GetThreats());
		assertEquals(0, scn.GetFreepsDeadCount());
		assertTrue(scn.ShadowPlayAvailable(breached));

		scn.ShadowPlayCard(breached);
		// Four of the five removed.
		assertEquals(1, scn.GetThreats());
		// The Free Peoples player chooses: any companion, the Ring-bearer among them; not an ally.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, gimli, boromir, arwen));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(elrond));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(gimli);
		assertInZone(Zone.DEAD, gimli);
		assertEquals(1, scn.GetFreepsDeadCount());

		// Placed in the dead pile, he was killed: the threat left over is a wound to place (CR 4.2 "dead pile", "threats").
		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertInDiscard(breached);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithThreeThreatsItCannotBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var breached = scn.GetShadowCard("breached");
		var soldier = scn.GetShadowCard("soldier");
		var band = scn.GetShadowCard("band");
		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("gimli"), scn.GetFreepsCard("boromir"));
		scn.MoveCardsToHand(breached);
		assertInHand(breached);

		scn.StartGame();
		scn.SetTwilight(5);
		scn.AddThreats(3);
		scn.MoveMinionsToTable(soldier, band);
		scn.FreepsRefreshDecision();
		assertEquals(3, scn.GetThreats());

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		assertEquals(3, scn.GetThreats());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(breached));
	}

	@Test
	public void WithOneSauronOrcBesideASauronUrukAndAWraithOrcItCannotBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var breached = scn.GetShadowCard("breached");
		var soldier = scn.GetShadowCard("soldier");
		var guard = scn.GetShadowCard("guard");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("gimli"), scn.GetFreepsCard("boromir"));
		scn.MoveCardsToHand(breached);
		assertInHand(breached);

		scn.StartGame();
		scn.SetTwilight(5);
		scn.AddThreats(4);
		scn.MoveMinionsToTable(soldier, guard, hound);
		scn.FreepsRefreshDecision();
		assertEquals(4, scn.GetThreats());

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		assertEquals(4, scn.GetThreats());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(breached));
	}
}
