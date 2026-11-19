package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_18_102_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("diadem", "18_102");
					put("theoden", "4_292");      // Theoden, Son of Thengel: [rohan] Man, cost 2; a regroup ability used only on purpose
					put("herugrim", "54_280");    // Herugrim: a [rohan] ARTIFACT (hand weapon), "Bearer must be Theoden"
					put("guard", "5_83");         // Household Guard: [rohan] Man companion; Valiant is its whole text
					put("herdsman1", "7_247");    // Rohirrim Herdsman: [rohan] Man ally, strength 4; a regroup ability used only on purpose
					put("herdsman2", "7_247");
					put("boromir", "1_97");       // Boromir, Son of Denethor: [gondor] Man; a skirmish ability used only on purpose
					put("merry", "103_85");       // Merry, Master Holbytla: [rohan] Hobbit, a Man only beside 3 [rohan] companions or Eowyn
					put("merrysword", "7_242");   // Merry's Sword: [rohan] possession, "Bearer must be Merry"; its maneuver ability is never used
					// Rider's Mount: [rohan] possession (mount); at the start of its bearer's skirmish each minion he
					// skirmishes exerts, which reads nothing here.
					put("mount", "4_287");
					put("spear", "4_288");        // Rider's Spear: [rohan] possession (hand weapon)
					put("chain", "4_278");        // Heavy Chain: [rohan] possession (armor)
					put("armor", "1_92");         // Armor: [gondor] possession, "Bearer must be a Man"
					put("coat", "1_101");         // Coat of Mail: [gondor] possession, "Bearer must be a Man"
					put("shield", "1_107");       // Great Shield: [gondor] possession, "Bearer must be a Man"
					// Protecting the Hall: [rohan] condition, "Bearer must be a [rohan] Man"; its assignment ability is
					// never used.
					put("hall", "11_149");
					put("cast", "18_94");         // Cast from the Hall: [rohan] condition, never played here
					put("savage", "1_151");       // Uruk Savage: strength 5, vitality 3; Damage +1 is its whole text
					put("runner", "1_178");       // Goblin Runner: strength 5
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RohirrimDiademStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Rohirrim Diadem
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: To play, spot Théoden.
		 * 	Skirmish: Spot a [rohan] Man bearing a possession and discard a [rohan] possession from hand to make that
		 * 	Man strength +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("diadem");

		assertEquals("Rohirrim Diadem", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilTheodenIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var diadem = scn.GetFreepsCard("diadem");
		var theoden = scn.GetFreepsCard("theoden");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(guard);
		scn.MoveCardsToHand(diadem, theoden);

		scn.StartGame();
		// A [rohan] Man companion who is no Theoden.
		assertInHand(diadem);
		assertFalse(scn.FreepsPlayAvailable(diadem));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPlayCard(theoden);
		assertTrue(scn.FreepsPlayAvailable(diadem));

		scn.FreepsPlayCard(diadem);
		assertInZone(Zone.SUPPORT, diadem);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SkirmishAbilitySpotsARohanManBearingAPossessionAndDiscardsARohanPossessionFromHandToMakeHimStrengthPlus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var diadem = scn.GetFreepsCard("diadem");
		var guard = scn.GetFreepsCard("guard");
		var herdsman1 = scn.GetFreepsCard("herdsman1");
		var herdsman2 = scn.GetFreepsCard("herdsman2");
		var boromir = scn.GetFreepsCard("boromir");
		var merry = scn.GetFreepsCard("merry");
		var frodo = scn.GetRingBearer();
		var mount = scn.GetFreepsCard("mount");
		var armor = scn.GetFreepsCard("armor");
		var hall = scn.GetFreepsCard("hall");
		var coat = scn.GetFreepsCard("coat");
		var merrysword = scn.GetFreepsCard("merrysword");
		var spear = scn.GetFreepsCard("spear");
		var chain = scn.GetFreepsCard("chain");
		var shield = scn.GetFreepsCard("shield");
		var cast = scn.GetFreepsCard("cast");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		// Two [rohan] companions (the Guard and Merry) and no Eowyn: Merry is no Man here.
		scn.MoveCompanionsToTable(guard, boromir, merry);
		scn.MoveAlliesToTable(herdsman1, herdsman2);
		scn.MoveCardsToSupportArea(diadem);
		// [rohan] Men bearing a possession: a companion bearing a [rohan] one, an ally bearing a [gondor] one.
		scn.AttachCardsTo(guard, mount);
		scn.AttachCardsTo(herdsman1, armor);
		// Not so: a [rohan] Man bearing a condition and no possession, a [gondor] Man and a [rohan] Hobbit
		// bearing possessions.
		scn.AttachCardsTo(herdsman2, hall);
		scn.AttachCardsTo(boromir, coat);
		scn.AttachCardsTo(merry, merrysword);
		scn.MoveCardsToHand(spear, chain, shield, cast);
		assertInZone(Zone.SUPPORT, diadem);
		assertAttachedTo(mount, guard);
		assertAttachedTo(armor, herdsman1);
		assertAttachedTo(hall, herdsman2);
		assertAttachedTo(coat, boromir);
		assertAttachedTo(merrysword, merry);
		assertInHand(spear, chain, shield, cast);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard, savage}, new PhysicalCardImpl[]{boromir, runner});
		scn.FreepsResolveSkirmish(guard);

		assertTrue(scn.FreepsActionAvailable(diadem));
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(4, scn.GetStrength(herdsman1));

		scn.FreepsUseCardAction(diadem);
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, herdsman1));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(herdsman2, boromir, merry, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(herdsman1);

		// Two [rohan] possessions in hand; not a [gondor] possession, nor a [rohan] condition.
		assertTrue(scn.FreepsHasCardChoiceAvailable(spear, chain));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(shield, cast));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(spear);

		assertInDiscard(spear);
		assertInHand(chain, shield, cast);
		// That Man, not the other one spotted could have been: an ally out of the skirmish is made +2.
		assertEquals(6, scn.GetStrength(herdsman1));
		assertEquals(6, scn.GetStrength(guard));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// The next skirmish is another phase: the bonus is gone.
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveSkirmish(boromir);
		assertEquals(4, scn.GetStrength(herdsman1));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWithNoRohanManBearingAPossession() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var diadem = scn.GetFreepsCard("diadem");
		var theoden = scn.GetFreepsCard("theoden");
		var herugrim = scn.GetFreepsCard("herugrim");
		var guard = scn.GetFreepsCard("guard");
		var herdsman = scn.GetFreepsCard("herdsman2");
		var hall = scn.GetFreepsCard("hall");
		var spear = scn.GetFreepsCard("spear");
		var savage = scn.GetShadowCard("savage");
		// Theoden bears an artifact, the Herdsman a condition, the Guard nothing; a [rohan] possession is in hand.
		scn.MoveCompanionsToTable(theoden, guard);
		scn.MoveAlliesToTable(herdsman);
		scn.MoveCardsToSupportArea(diadem);
		scn.AttachCardsTo(theoden, herugrim);
		scn.AttachCardsTo(herdsman, hall);
		scn.MoveCardsToHand(spear);
		assertInZone(Zone.SUPPORT, diadem);
		assertAttachedTo(herugrim, theoden);
		assertAttachedTo(hall, herdsman);
		assertInHand(spear);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(theoden, savage);
		scn.FreepsResolveSkirmish(theoden);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(diadem));
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWithNoRohanPossessionInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var diadem = scn.GetFreepsCard("diadem");
		var guard = scn.GetFreepsCard("guard");
		var mount = scn.GetFreepsCard("mount");
		var shield = scn.GetFreepsCard("shield");
		var cast = scn.GetFreepsCard("cast");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(guard);
		scn.MoveCardsToSupportArea(diadem);
		scn.AttachCardsTo(guard, mount);
		// A [gondor] possession and a [rohan] condition in hand.
		scn.MoveCardsToHand(shield, cast);
		assertInZone(Zone.SUPPORT, diadem);
		assertAttachedTo(mount, guard);
		assertInHand(shield, cast);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, savage);
		scn.FreepsResolveSkirmish(guard);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(diadem));
	}
}
