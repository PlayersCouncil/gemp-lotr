package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_10_004_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aegnor", "10_4");
					put("haldir", "1_48");      // Haldir, Elf of the Golden Wood: unbound [elven] Elf companion, no archer, strength 5; +2 only at sites 6 to 8
					put("gimli", "1_13");       // Gimli, Son of Gloin: unbound [dwarven] Dwarf companion, no archer; a skirmish ability used only on purpose
					put("legolas", "1_50");     // Legolas, Greenleaf: an archer companion; an archery ability only
					put("orophin", "1_56");     // Orophin, Lorien Bowman: an archer ally, home site 6; an archery ability only
					put("silinde", "1_60");     // Silinde, Elf of Mirkwood: [elven] Elf ally, home site 3, where she skirmishes

					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1 only
					put("scout", "1_191");      // Moria Scout: [moria] Orc, strength 6, vitality 2; a play trigger only
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc, strength 7, vitality 2; a skirmish ability used only on purpose
					put("marksman", "1_176");   // Goblin Marksman: a Shadow archer minion and nothing else
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AegnorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Aegnor, Elven Escort
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Elf
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: <b>Skirmish:</b> Exert Aegnor to make a minion skirmishing an unbound [elven] companion strength -1 for each archer you spot.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("aegnor");

		assertEquals("Aegnor", card.getBlueprint().getTitle());
		assertEquals("Elven Escort", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void ExertsAegnorToMakeAMinionSkirmishingAnUnboundElvenCompanionStrengthMinus1ForEachArcherSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aegnor = scn.GetFreepsCard("aegnor");
		var haldir = scn.GetFreepsCard("haldir");
		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		var orophin = scn.GetFreepsCard("orophin");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var scout = scn.GetShadowCard("scout");
		var soldier = scn.GetShadowCard("soldier");
		var marksman = scn.GetShadowCard("marksman");
		scn.MoveCompanionsToTable(aegnor, haldir, gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage, scout, soldier);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{haldir, troop},
				new PhysicalCardImpl[]{aegnor, scout},
				new PhysicalCardImpl[]{gimli, soldier}
		);
		scn.ShadowAssignToMinions(haldir, savage);
		// The archers go down once the archery phase is past: an archer companion, an archer ally and the Shadow
		// player's archer minion.
		scn.MoveCompanionsToTable(legolas);
		scn.MoveAlliesToTable(orophin);
		scn.MoveMinionsToTable(marksman);

		scn.FreepsResolveSkirmish(gimli);
		assertEquals(0, scn.GetWoundsOn(aegnor));
		assertEquals(7, scn.GetStrength(soldier));
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(6, scn.GetStrength(scout));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(aegnor));

		// In Gimli's skirmish the Soldier fights a companion who is not [elven], and the minions assigned to Haldir
		// and Aegnor are not skirmishing yet: the exertion buys nothing.
		scn.FreepsUseCardAction(aegnor);
		assertEquals(1, scn.GetWoundsOn(aegnor));
		scn.FreepsChoose("3");
		assertEquals(7, scn.GetStrength(soldier));
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(6, scn.GetStrength(scout));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(haldir);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		scn.FreepsUseCardAction(aegnor);
		assertEquals(2, scn.GetWoundsOn(aegnor));

		// The archers are the player's to count, from none to the three there are (Legolas, Orophin and the
		// Marksman), filled in with all of them; Aegnor, Haldir and Gimli are no archers.
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(3, scn.FreepsGetChoiceMax());
		assertEquals(Integer.valueOf(3), scn.FreepsGetChoiceDefault());
		scn.FreepsChoose("2");

		// Both minions skirmishing Haldir; not the Scout, assigned to Aegnor, nor the Soldier, whose skirmish is over.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(scout, soldier, marksman));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(troop);

		// 13 - 2 named archers
		assertEquals(11, scn.GetStrength(troop));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(6, scn.GetStrength(scout));
		// Read again at the next decision of the same skirmish.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(11, scn.GetStrength(troop));

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(aegnor);

		// A skirmish ability lasts for the skirmish phase it is used in: the Troop is back to 13 in the next one.
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void InHisOwnSkirmishWithNoArcherTheExertionBuysNothingAndWithOneTheMinionIsStrengthMinus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aegnor = scn.GetFreepsCard("aegnor");
		var orophin = scn.GetFreepsCard("orophin");
		var scout = scn.GetShadowCard("scout");
		scn.MoveCompanionsToTable(aegnor);

		scn.StartGame();
		scn.MoveMinionsToTable(scout);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aegnor, scout);
		scn.FreepsResolveSkirmish(aegnor);

		assertEquals(0, scn.GetWoundsOn(aegnor));
		assertEquals(6, scn.GetStrength(scout));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(aegnor));

		// No archer to spot: the ability is offered and Aegnor exerts, and the Scout keeps its strength.
		scn.FreepsUseCardAction(aegnor);
		assertEquals(1, scn.GetWoundsOn(aegnor));
		assertEquals(6, scn.GetStrength(scout));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// One archer (an ally) on the table; the Scout skirmishing Aegnor himself is the one candidate.
		scn.MoveAlliesToTable(orophin);
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		scn.FreepsUseCardAction(aegnor);
		assertEquals(2, scn.GetWoundsOn(aegnor));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("1");

		assertEquals(5, scn.GetStrength(scout));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AMinionSkirmishingAnElvenAllyIsNotWeakened() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aegnor = scn.GetFreepsCard("aegnor");
		var silinde = scn.GetFreepsCard("silinde");
		var orophin = scn.GetFreepsCard("orophin");
		var savage = scn.GetShadowCard("savage");
		var scout = scn.GetShadowCard("scout");
		scn.MoveCompanionsToTable(aegnor);
		scn.MoveAlliesToTable(silinde);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(savage, scout);
		// The fellowship moves to site 3, Silinde's home, where she can skirmish.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{silinde, savage},
				new PhysicalCardImpl[]{aegnor, scout}
		);
		scn.MoveAlliesToTable(orophin);
		scn.FreepsResolveSkirmish(silinde);

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(aegnor));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(6, scn.GetStrength(scout));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Silinde is an [elven] Elf but an ally, not a companion: with an archer to count, the exertion buys
		// nothing.
		scn.FreepsUseCardAction(aegnor);
		assertEquals(1, scn.GetWoundsOn(aegnor));
		scn.FreepsChoose("1");
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(6, scn.GetStrength(scout));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWhenAegnorIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aegnor = scn.GetFreepsCard("aegnor");
		var orophin = scn.GetFreepsCard("orophin");
		var scout = scn.GetShadowCard("scout");
		scn.MoveCompanionsToTable(aegnor);
		scn.MoveAlliesToTable(orophin);
		// Vitality 3 with 2 wounds: Aegnor cannot exert.
		scn.AddWoundsToChar(aegnor, 2);

		scn.StartGame();
		assertEquals(2, scn.GetWoundsOn(aegnor));
		scn.MoveMinionsToTable(scout);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aegnor, scout);
		scn.FreepsResolveSkirmish(aegnor);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(aegnor));
		assertEquals(2, scn.GetWoundsOn(aegnor));
	}
}
