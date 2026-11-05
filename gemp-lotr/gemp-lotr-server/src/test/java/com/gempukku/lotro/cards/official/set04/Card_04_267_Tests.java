package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_267_Tests
{

	// Site 1 is Bag End (no discount of its own); site 2 is Ettenmoors, whose skirmish ability lets either player
	// exert his own companion or minion.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("eomer", "4_267");
					put("guard", "5_83");       // Household Guard: a [rohan] Man companion, strength 6, valiant and nothing else
					put("guma", "4_277");       // Guma, Plains Farmer: a [rohan] Man ALLY, cost 1; a fellowship ability used only on purpose
					put("merry", "103_85");     // Merry, Master Holbytla: a [rohan] Hobbit companion, a Man only while 3 [rohan] companions or Eowyn can be spotted
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion; a maneuver ability used only on purpose
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc minion, vitality 3, no text
					put("savage", "1_151");     // Uruk Savage: an [isengard] Uruk-hai minion, vitality 3; damage +1 is its whole text
					put("otsea", "1_235");      // Ulaire Otsea, Lieutenant of Morgul: a Nazgul minion, strength 9, vitality 3; a Shadow ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EomerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Éomer, Third Marshal of Riddermark
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 7
		 * Vitality: 3
		 * Resistance: 6
		 * Signet: Theoden
		 * Game Text: While you can spot a [rohan] Man, Éomer's twilight cost is -1.
		 * 	Éomer is strength +2 for each wound on each minion he is skirmishing.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("eomer");

		assertEquals("Éomer", card.getBlueprint().getTitle());
		assertEquals("Third Marshal of Riddermark", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.THEODEN, card.getBlueprint().getSignet());
	}

	@Test
	public void WithARohanManCompanionToSpotEomerIsPlayedForTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eomer = scn.GetFreepsCard("eomer");
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(eomer, aragorn);
		scn.MoveCompanionsToTable(guard);

		scn.StartGame();

		assertEquals(2, scn.GetTwilightCost(eomer));    // 3 - 1
		// Another companion in the hand, beside the [rohan] Man he spots, costs what it is printed at.
		assertEquals(4, scn.GetTwilightCost(aragorn));
		assertEquals(0, scn.GetTwilight());

		scn.FreepsPlayCard(eomer);

		// The text names Éomer himself, so it applies to him as he is played (CR 4.2 "modifier").
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, eomer);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ARohanHobbitAndAGondorManDoNotLowerHisCostAndARohanManAllyPlayedThenDoes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eomer = scn.GetFreepsCard("eomer");
		var guma = scn.GetFreepsCard("guma");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(eomer, guma);
		scn.MoveCompanionsToTable(merry, aragorn);

		scn.StartGame();

		// A [rohan] companion who is no Man, and a Man who is not [rohan].
		assertFalse(scn.IsRace(merry, Race.MAN));
		assertEquals(3, scn.GetTwilightCost(eomer));

		scn.FreepsPlayCard(guma);

		assertInZone(Zone.SUPPORT, guma);
		assertEquals(2, scn.GetTwilightCost(eomer));    // 3 - 1
		int pool = scn.GetTwilight();

		scn.FreepsPlayCard(eomer);

		assertEquals(pool + 2, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, eomer);
		// Two [rohan] companions: Merry is still no Man.
		assertFalse(scn.IsRace(merry, Race.MAN));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void HeIsStrengthPlusTwoForEachWoundOnEachMinionHeIsSkirmishingAndForNoOtherWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eomer = scn.GetFreepsCard("eomer");
		var guard = scn.GetFreepsCard("guard");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var otsea = scn.GetShadowCard("otsea");
		scn.MoveCompanionsToTable(eomer, guard);

		scn.StartGame();
		scn.FreepsPass();

		// At Ettenmoors, in the Shadow phase.
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.MoveMinionsToTable(troop, savage, otsea);
		scn.AddWoundsToChar(troop, 1);
		scn.AddWoundsToChar(savage, 2);
		scn.AddWoundsToChar(otsea, 2);
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(2, scn.GetWoundsOn(savage));
		assertEquals(2, scn.GetWoundsOn(otsea));
		assertEquals(7, scn.GetStrength(eomer));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{eomer, troop}, new PhysicalCardImpl[]{guard, otsea});
		// The minion left over: the Shadow player puts the Uruk-hai on Éomer as well.
		scn.ShadowAssignToMinions(eomer, savage);

		// Assigned to two wounded minions, but not yet skirmishing them.
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertEquals(7, scn.GetStrength(eomer));

		scn.FreepsResolveSkirmish(guard);

		// In the other skirmish: the Nazgul's two wounds are not on a minion Éomer is skirmishing, and the Household
		// Guard, skirmishing that wounded minion, keeps his own strength.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(7, scn.GetStrength(eomer));
		assertEquals(6, scn.GetStrength(guard));

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(eomer);

		// 1 wound on the Orc and 2 on the Uruk-hai: 7 + 2 x 3.  The Nazgul, still on the table with its 2 wounds,
		// fought elsewhere.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertInZone(Zone.SHADOW_CHARACTERS, otsea);
		assertEquals(2, scn.GetWoundsOn(otsea));
		assertEquals(13, scn.GetStrength(eomer));

		// The Shadow player exerts the Orc by Ettenmoors: one more wound on a minion Éomer is skirmishing.  The
		// Uruk-hai and the Nazgul, at 2 wounds of 3, are exhausted, so the Orc is exerted unasked.
		scn.FreepsPass();
		scn.ShadowUseCardAction(scn.GetCurrentSite());

		assertEquals(2, scn.GetWoundsOn(troop));
		assertEquals(15, scn.GetStrength(eomer));     // 7 + 2 x 4
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
