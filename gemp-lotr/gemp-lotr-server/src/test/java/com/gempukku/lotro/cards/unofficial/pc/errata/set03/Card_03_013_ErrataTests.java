package com.gempukku.lotro.cards.unofficial.pc.errata.set03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_013_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("elrond", "53_13");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, vitality 4
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: [dwarven] Dwarf ally, home site 3, vitality 2
					put("bilbo", "1_284");      // Bilbo, Retired Adventurer: [shire] Hobbit ally, home site 3, vitality 3
					put("silinde", "1_60");     // Silinde, Elf of Mirkwood: [elven] Elf ally, home site 3, vitality 2
					put("orophin", "1_56");     // Orophin, Lorien Bowman: [elven] Elf ally, home site 6, vitality 3
					put("deor", "7_222");       // Deor: [rohan] Man ally, home site 3 of the Tower block, vitality 2
					put("troop", "1_177");      // Goblin Patrol Troop: minion with no text, vitality 3
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ElrondStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Elrond, Herald to Gil-galad
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 4
		 * Type: Ally
		 * Subtype: Elf
		 * Strength: 8
		 * Vitality: 4
		 * Home: 3
		 * Game Text: At the start of each of your turns, you may spot an ally whose home is site 3 to heal that ally
		 * up to 2 times.  Add (1) per wound healed.
		 * Regroup: Exert Elrond twice to heal a companion.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("elrond");

		assertEquals("Elrond", card.getBlueprint().getTitle());
		assertEquals("Herald to Gil-galad", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.FELLOWSHIP, 3)));
	}

	@Test
	public void AtTheStartOfTurnHealsAChosenAllyOfSite3TwiceAndAddsTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elrond = scn.GetFreepsCard("elrond");
		var aragorn = scn.GetFreepsCard("aragorn");
		var thrarin = scn.GetFreepsCard("thrarin");
		var bilbo = scn.GetFreepsCard("bilbo");
		var orophin = scn.GetFreepsCard("orophin");
		var deor = scn.GetFreepsCard("deor");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(elrond, thrarin, bilbo, orophin, deor);
		scn.AddWoundsToChar(elrond, 3);
		scn.AddWoundsToChar(thrarin, 1);
		scn.AddWoundsToChar(bilbo, 2);
		scn.AddWoundsToChar(orophin, 1);
		scn.AddWoundsToChar(deor, 1);
		scn.AddWoundsToChar(aragorn, 1);

		scn.StartGame();

		assertEquals(Phase.BETWEEN_TURNS, scn.GetCurrentPhase());
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(elrond));
		scn.FreepsChooseOptionalTrigger(elrond);

		// Elrond himself, a Dwarf and a Hobbit: allies of home site 3.  Not an ally of home site 6, an ally of
		// home site 3 of the Tower block, or a companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(elrond, thrarin, bilbo));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(orophin, deor, aragorn));
		scn.FreepsChooseCard(elrond);

		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("2");

		// Three wounds, healed twice.
		assertEquals(1, scn.GetWoundsOn(elrond));
		assertEquals(2, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(thrarin));
		assertEquals(2, scn.GetWoundsOn(bilbo));
		assertEquals(1, scn.GetWoundsOn(orophin));
		assertEquals(1, scn.GetWoundsOn(deor));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void HealingAnAllyOnceOfTwoWoundsAddsOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elrond = scn.GetFreepsCard("elrond");
		var bilbo = scn.GetFreepsCard("bilbo");
		scn.MoveAlliesToTable(elrond, bilbo);
		scn.AddWoundsToChar(bilbo, 2);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		scn.FreepsChooseOptionalTrigger(elrond);
		scn.FreepsChooseCard(bilbo);
		scn.FreepsChoose("1");

		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnAllyWithOneWoundIsHealedOnceAndAddsOneHoweverManyHealsAreNamed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elrond = scn.GetFreepsCard("elrond");
		var thrarin = scn.GetFreepsCard("thrarin");
		var bilbo = scn.GetFreepsCard("bilbo");
		scn.MoveAlliesToTable(elrond, thrarin, bilbo);
		scn.AddWoundsToChar(thrarin, 1);
		scn.AddWoundsToChar(bilbo, 2);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		scn.FreepsChooseOptionalTrigger(elrond);
		scn.FreepsChooseCard(thrarin);
		// The most the player may name.
		scn.FreepsChoose(String.valueOf(scn.FreepsGetChoiceMax()));

		assertEquals(0, scn.GetWoundsOn(thrarin));
		assertEquals(1, scn.GetTwilight());
		assertEquals(2, scn.GetWoundsOn(bilbo));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DecliningHealsNothingAndAddsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elrond = scn.GetFreepsCard("elrond");
		var bilbo = scn.GetFreepsCard("bilbo");
		scn.MoveAlliesToTable(elrond, bilbo);
		scn.AddWoundsToChar(bilbo, 2);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(elrond));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(2, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoWoundedAllyOfSite3TheTriggerIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elrond = scn.GetFreepsCard("elrond");
		var aragorn = scn.GetFreepsCard("aragorn");
		var silinde = scn.GetFreepsCard("silinde");
		var orophin = scn.GetFreepsCard("orophin");
		var deor = scn.GetFreepsCard("deor");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(elrond, silinde, orophin, deor);
		// Wounded, but none an ally of home site 3.
		scn.AddWoundsToChar(orophin, 1);
		scn.AddWoundsToChar(deor, 1);
		scn.AddWoundsToChar(aragorn, 1);

		scn.StartGame();

		assertFalse(scn.FreepsHasOptionalTriggerAvailable(elrond));
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void RegroupActionExertsElrondTwiceToHealACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var elrond = scn.GetFreepsCard("elrond");
		var aragorn = scn.GetFreepsCard("aragorn");
		var thrarin = scn.GetFreepsCard("thrarin");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(thrarin);

		scn.StartGame();
		// Elrond is put down after the start of the turn, so that his trigger is not offered.
		scn.MoveAlliesToTable(elrond);
		scn.AddWoundsToChar(elrond, 1);
		scn.AddWoundsToChar(frodo, 2);
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(thrarin, 1);
		// With no minion in play the game goes from the Shadow phase straight to regroup.
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(troop);
		scn.AddWoundsToChar(troop, 1);

		assertTrue(scn.FreepsActionAvailable(elrond));
		scn.FreepsUseCardAction(elrond);
		assertEquals(3, scn.GetWoundsOn(elrond));

		// Two companions of different races; not a wounded ally, Elrond himself or a minion.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(thrarin, elrond, troop));
		scn.FreepsChooseCard(aragorn);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(thrarin));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(3, scn.GetWoundsOn(elrond));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void RegroupActionIsNotOfferedWhenElrondCannotExertTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var elrond = scn.GetFreepsCard("elrond");

		scn.StartGame();
		scn.MoveAlliesToTable(elrond);
		// Vitality 4 with 2 wounds: he can exert once, not twice.
		scn.AddWoundsToChar(elrond, 2);
		scn.AddWoundsToChar(frodo, 2);
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsActionAvailable(elrond));
		assertEquals(2, scn.GetWoundsOn(elrond));
		assertEquals(2, scn.GetWoundsOn(frodo));
	}
}
