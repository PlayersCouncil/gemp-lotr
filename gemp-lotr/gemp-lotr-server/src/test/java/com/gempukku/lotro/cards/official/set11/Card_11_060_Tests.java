package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_060_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("quality", "11_60");
					put("quality2", "11_60");

					// [gondor] companions, by resistance.
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4, resistance 6; a maneuver ability used only on purpose
					put("strider", "11_54");    // Aragorn, Strider: strength 8, vitality 4, resistance 8; adds (1) at each move
					put("knight1", "5_35");     // Gondorian Knight: a Man, strength 6, vitality 3, resistance 6; Knight is its whole text
					put("knight2", "5_35");
					// Guard of the White Tree: a Man, strength 7, vitality 3, resistance 5.  Its skirmish ability
					// discards a [gondor] condition from the support area, and there is none.
					put("guard1", "13_67");
					put("guard2", "13_67");
					// Pippin, Trollslayer: a [gondor] companion who is a Hobbit and no Man; vitality 4, resistance 6.
					// His skirmish ability is about a minion he is skirmishing, and he never fights here.
					put("pippin", "103_31");

					// Who may not pay.
					put("rohirrim", "5_83");    // Household Guard: a [rohan] Man, a companion of strength 6, vitality 3, resistance 6; Valiant is its whole text
					// Gondorian Merchant: the one [gondor] ally, a Man of strength 6 and vitality 1.  His maneuver
					// ability needs initiative and 2 cards discarded from hand.
					put("merchant", "7_97");
					// Narsil, Blade of the Faithful: [gondor] artifact, vitality +1, "Bearer must be a [gondor] Man".
					// With it the Merchant has the vitality to exert.  Its response answers a wound to its bearer.
					put("narsil", "9_34");
					// One Whom Men Would Follow: "Maneuver: Exert Aragorn to spot an ally. Until the regroup phase,
					// that ally is strength +2 and participates in archery fire and skirmishes."
					put("follow", "1_109");

					put("footman", "11_82");    // Footman of Dunland: [men] Man, strength 7, vitality 3; the current site gains plains
					put("savage1", "1_151");    // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; Damage +1 is its whole text
					put("savage2", "1_151");
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, vitality 3, no text
					// The Mouth of Sauron, Bearer of Dread News: strength 9, vitality 3, lurker.  "Response: If a
					// Free Peoples event is played, exert this minion to cancel that event."
					put("mouth", "103_70");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheHighestQualityStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: The Highest Quality
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Exert any number of [gondor] companions who have total resistance 12
		 * or more to make a minion skirmishing a [gondor] companion strength -3 for each
		 * companion exerted this way.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("quality");

		assertEquals("The Highest Quality", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void TwoGondorCompanionsOfTotalResistanceTwelveExertToMakeAMinionSkirmishingAGondorCompanionStrengthMinusSix() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var quality = scn.GetFreepsCard("quality");
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight1 = scn.GetFreepsCard("knight1");
		var knight2 = scn.GetFreepsCard("knight2");
		var pippin = scn.GetFreepsCard("pippin");
		var rohirrim = scn.GetFreepsCard("rohirrim");
		var merchant = scn.GetFreepsCard("merchant");
		var narsil = scn.GetFreepsCard("narsil");
		scn.MoveCompanionsToTable(aragorn, knight1, knight2, pippin, rohirrim);
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(merchant, narsil);
		scn.MoveCardsToHand(quality);

		var footman = scn.GetShadowCard("footman");
		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");

		scn.StartGame();
		scn.MoveMinionsToTable(footman, savage1, savage2);
		scn.SkipToAssignments();
		// Aragorn takes the Footman and, from the Shadow player, a Savage; the other Savage waits for a Knight.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, footman}, new PhysicalCardImpl[]{knight2, savage2});
		scn.ShadowAssignToMinions(aragorn, savage1);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(6, scn.GetResistance(knight1));
		assertEquals(6, scn.GetResistance(knight2));
		// The Merchant has the vitality to exert, so only his being an ally keeps him out.
		assertEquals(2, scn.GetVitality(merchant));
		assertEquals(7, scn.GetStrength(footman));
		assertEquals(5, scn.GetStrength(savage1));
		assertEquals(5, scn.GetStrength(savage2));
		var pool = scn.GetTwilight();
		assertTrue(scn.FreepsPlayAvailable(quality));

		scn.FreepsPlayCard(quality);
		// [gondor] companions, the Hobbit among them and the one who is fighting; not the [rohan] Man, the
		// [gondor] ally or the Ring-bearer.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, knight1, knight2, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(rohirrim, merchant, frodo));

		// One Knight is resistance 6: no payment, and the choice is put again with nobody exerted.
		scn.FreepsChooseCards(knight1);
		assertEquals(0, scn.GetWoundsOn(knight1));
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, knight1, knight2, pippin));

		// Two Knights are 12.
		scn.FreepsChooseCards(knight1, knight2);
		assertEquals(1, scn.GetWoundsOn(knight1));
		assertEquals(1, scn.GetWoundsOn(knight2));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(pippin));

		// Either minion fighting Aragorn; not the one whose skirmish with a [gondor] companion has not begun.
		assertTrue(scn.FreepsHasCardChoiceAvailable(footman, savage1));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(savage2));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(footman);

		// 7 - 3 for each of the two Knights
		assertEquals(1, scn.GetStrength(footman));
		assertEquals(5, scn.GetStrength(savage1));
		assertEquals(5, scn.GetStrength(savage2));
		assertEquals(pool + 2, scn.GetTwilight());
		assertInDiscard(quality);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Aragorn's 8 beats 1 + 5.  In the next skirmish the Footman is himself again.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(footman));
		scn.FreepsResolveSkirmish(knight2);
		assertEquals(7, scn.GetStrength(footman));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void TheHighestQualityExerts12ResWorthOfMenToDebuffAMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var quality = scn.GetFreepsCard("quality");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var knight1 = scn.GetFreepsCard("knight1");
		scn.MoveCardsToHand(quality);
		scn.MoveCompanionsToTable(guard1, guard2, knight1);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard1, troop);
		scn.FreepsResolveSkirmish(guard1);

		assertEquals(5, scn.GetResistance(guard1));
		assertEquals(5, scn.GetResistance(guard2));
		assertEquals(6, scn.GetResistance(knight1));
		assertEquals(0, scn.GetWoundsOn(guard1));
		assertEquals(0, scn.GetWoundsOn(guard2));
		assertEquals(0, scn.GetWoundsOn(knight1));
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.FreepsPlayAvailable(quality));

		scn.FreepsPlayCard(quality);
		// 5 + 5 and 5 + 6 fall short: each is asked for again, and nobody has exerted.
		scn.FreepsChooseCards(guard1, guard2);
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard1, guard2, knight1));
		scn.FreepsChooseCards(guard1, knight1);
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard1, guard2, knight1));
		assertEquals(0, scn.GetWoundsOn(guard1));
		assertEquals(0, scn.GetWoundsOn(knight1));

		scn.FreepsChooseCards(guard1, guard2, knight1);
		assertEquals(1, scn.GetWoundsOn(guard1));
		assertEquals(1, scn.GetWoundsOn(guard2));
		assertEquals(1, scn.GetWoundsOn(knight1));
		// 13 - 3 for each of the three
		assertEquals(4, scn.GetStrength(troop));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileABurdenLeavesTheGondorCompanionsOneShortOfTwelve() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var quality = scn.GetFreepsCard("quality");
		var strider = scn.GetFreepsCard("strider");
		var guard1 = scn.GetFreepsCard("guard1");
		var rohirrim = scn.GetFreepsCard("rohirrim");
		scn.MoveCompanionsToTable(strider, guard1, rohirrim);
		scn.MoveCardsToHand(quality);

		var savage1 = scn.GetShadowCard("savage1");

		scn.StartGame();
		scn.AddBurdens(1);
		scn.MoveMinionsToTable(savage1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(strider, savage1);
		scn.FreepsResolveSkirmish(strider);

		// 8 and 5 are printed; the burden takes 1 from each.  The [rohan] Man's 5 or Frodo's 9 would make up
		// the 12, and neither is a [gondor] companion.
		assertEquals(7, scn.GetResistance(strider));
		assertEquals(4, scn.GetResistance(guard1));
		assertEquals(5, scn.GetResistance(rohirrim));
		assertEquals(9, scn.GetResistance(frodo));
		assertEquals(0, scn.GetWoundsOn(strider));
		assertEquals(0, scn.GetWoundsOn(guard1));
		assertInZone(Zone.HAND, quality);
		assertFalse(scn.FreepsPlayAvailable(quality));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedOnceTheCompanionsAbleToExertFallShortOfTwelve() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var quality = scn.GetFreepsCard("quality");
		var knight1 = scn.GetFreepsCard("knight1");
		var knight2 = scn.GetFreepsCard("knight2");
		scn.MoveCompanionsToTable(knight1, knight2);
		scn.MoveCardsToHand(quality);

		var footman = scn.GetShadowCard("footman");
		var savage1 = scn.GetShadowCard("savage1");

		scn.StartGame();
		scn.AddWoundsToChar(knight1, 1);
		scn.MoveMinionsToTable(footman, savage1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{knight1, footman}, new PhysicalCardImpl[]{knight2, savage1});

		// Two Knights who can both exert are exactly 12.
		scn.FreepsResolveSkirmish(knight1);
		assertEquals(6, scn.GetResistance(knight1));
		assertEquals(6, scn.GetResistance(knight2));
		assertTrue(scn.FreepsPlayAvailable(quality));

		// The first Knight loses, 6 against 7: vitality 3 with 2 wounds, he is exhausted.
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(knight1));
		assertEquals(1, scn.GetVitality(knight1));

		// The two still have 12 between them, but only one of them can exert.
		scn.FreepsResolveSkirmish(knight2);
		assertEquals(6, scn.GetResistance(knight1));
		assertEquals(6, scn.GetResistance(knight2));
		assertInZone(Zone.HAND, quality);
		assertFalse(scn.FreepsPlayAvailable(quality));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AMinionSkirmishingACompanionOfAnotherCultureOrAGondorAllyIsNotWeakened() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var quality = scn.GetFreepsCard("quality");
		var quality2 = scn.GetFreepsCard("quality2");
		var follow = scn.GetFreepsCard("follow");
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight1 = scn.GetFreepsCard("knight1");
		var knight2 = scn.GetFreepsCard("knight2");
		var rohirrim = scn.GetFreepsCard("rohirrim");
		var merchant = scn.GetFreepsCard("merchant");
		scn.MoveCompanionsToTable(aragorn, knight1, knight2, rohirrim);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToHand(quality, quality2, follow);

		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");

		scn.StartGame();
		scn.MoveMinionsToTable(savage1, savage2);
		scn.SkipToPhase(Phase.MANEUVER);
		// Aragorn exerts, and the Merchant, the only ally, fights until the regroup phase at strength 6 + 2.
		scn.FreepsPlayCard(follow);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(8, scn.GetStrength(merchant));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{rohirrim, savage1}, new PhysicalCardImpl[]{merchant, savage2});

		// The [rohan] Man is no [gondor] companion: the Knights exert, and there is no minion to weaken.
		scn.FreepsResolveSkirmish(rohirrim);
		assertTrue(scn.FreepsPlayAvailable(quality));
		scn.FreepsPlayCard(quality);
		scn.FreepsChooseCards(knight1, knight2);
		assertEquals(1, scn.GetWoundsOn(knight1));
		assertEquals(1, scn.GetWoundsOn(knight2));
		assertEquals(5, scn.GetStrength(savage1));
		assertEquals(5, scn.GetStrength(savage2));
		assertInDiscard(quality);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// The [rohan] Man's 6 beats 5.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(savage1));

		// The Merchant is [gondor] and no companion: the same again.
		scn.FreepsResolveSkirmish(merchant);
		assertTrue(scn.FreepsPlayAvailable(quality2));
		scn.FreepsPlayCard(quality2);
		scn.FreepsChooseCards(knight1, knight2);
		assertEquals(2, scn.GetWoundsOn(knight1));
		assertEquals(2, scn.GetWoundsOn(knight2));
		assertEquals(5, scn.GetStrength(savage1));
		assertEquals(5, scn.GetStrength(savage2));
		assertInDiscard(quality2);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void TheCompanionsExertBeforeTheShadowPlayerMayCancelTheEventAndStayExerted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var quality = scn.GetFreepsCard("quality");
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight1 = scn.GetFreepsCard("knight1");
		var knight2 = scn.GetFreepsCard("knight2");
		scn.MoveCompanionsToTable(aragorn, knight1, knight2);
		scn.MoveCardsToHand(quality);

		var footman = scn.GetShadowCard("footman");
		var mouth = scn.GetShadowCard("mouth");

		scn.StartGame();
		scn.MoveMinionsToTable(footman, mouth);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, footman);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		var pool = scn.GetTwilight();
		scn.FreepsPlayCard(quality);
		scn.FreepsChooseCards(knight1, knight2);

		// The exertions stand before the "to": they are paid by the time the Shadow player may answer the event.
		assertEquals(1, scn.GetWoundsOn(knight1));
		assertEquals(1, scn.GetWoundsOn(knight2));
		assertTrue(scn.ShadowActionAvailable(mouth));
		scn.ShadowUseCardAction(mouth);
		assertEquals(1, scn.GetWoundsOn(mouth));

		// Cancelled: the Footman is not weakened, and what was paid stays paid.
		assertEquals(7, scn.GetStrength(footman));
		assertEquals(1, scn.GetWoundsOn(knight1));
		assertEquals(1, scn.GetWoundsOn(knight2));
		assertEquals(pool + 2, scn.GetTwilight());
		assertInDiscard(quality);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
