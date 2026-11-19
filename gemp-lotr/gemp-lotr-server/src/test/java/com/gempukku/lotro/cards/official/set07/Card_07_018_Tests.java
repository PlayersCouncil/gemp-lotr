package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_018_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bow", "7_18");
					// Legolas, Companion of the Ring: an Elf companion, strength 6, vitality 3, no archer (an archer
					// would stop the skippers); his text needs a battleground or forest, which no site of this path is
					put("legolas", "11_21");
					// Haldir, Elf of the Golden Wood: an Elf companion who is no Legolas, strength 5; +2 only at sites 6 to 8
					put("haldir", "1_48");
					// Defiance: an [elven] Skirmish event, cost 1, "Make an Elf strength +2 (or +4 if skirmishing a Nazgûl)"
					put("defiance1", "1_37");
					put("defiance2", "1_37");
					put("defiance3", "1_37");
					put("defiance4", "1_37");
					// Swordarm of the White Tower: a [gondor] Skirmish event, cost 0, "Make a [gondor] companion
					// strength +2 ..."; no [gondor] companion is in play, so it is played for nothing
					put("swordarm", "1_116");
					// Night Without End: an [elven] Response event, cost 1, "If an Elf wins a skirmish, exert that Elf
					// to liberate a site or wound a minion"
					put("night", "4_79");
					// Morgul Hound: a [wraith] Orc, strength 5, vitality 2; +4 and fierce only at 2 wounds on the Ring-bearer
					put("hound1", "7_192");
					put("hound2", "7_192");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BowoftheGaladhrimStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Bow of the Galadhrim, Gift of Galadriel
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Ranged weapon
		 * Strength: 1
		 * Game Text: Bearer must be Legolas.<br>Each time you play an [elven] skirmish event during a skirmish involving Legolas, you may heal him.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("bow");

		assertEquals("Bow of the Galadhrim", card.getBlueprint().getTitle());
		assertEquals("Gift of Galadriel", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.RANGED_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
	}

	@Test
	public void CannotBePlayedOnAnotherElfCompanionAndIsPlayedOnLegolasOnceHeIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetFreepsCard("bow");
		var legolas = scn.GetFreepsCard("legolas");
		var haldir = scn.GetFreepsCard("haldir");
		scn.MoveCompanionsToTable(haldir);
		scn.MoveCardsToHand(bow, legolas);

		scn.StartGame();
		assertInHand(bow, legolas);

		// No Legolas: Haldir, an Elf companion, and Frodo cannot bear it.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(bow));

		scn.FreepsPlayCard(legolas);
		assertTrue(scn.FreepsPlayAvailable(bow));
		scn.FreepsPlayCard(bow);
		// The one Legolas there can be takes it unasked.
		assertAttachedTo(bow, legolas);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachElvenSkirmishEventPlayedInLegolassSkirmishMayHealHimUntilHeHasNoWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetFreepsCard("bow");
		var legolas = scn.GetFreepsCard("legolas");
		var haldir = scn.GetFreepsCard("haldir");
		var defiance1 = scn.GetFreepsCard("defiance1");
		var defiance2 = scn.GetFreepsCard("defiance2");
		var defiance3 = scn.GetFreepsCard("defiance3");
		var defiance4 = scn.GetFreepsCard("defiance4");
		var hound1 = scn.GetShadowCard("hound1");
		var hound2 = scn.GetShadowCard("hound2");
		scn.MoveCompanionsToTable(legolas, haldir);
		scn.AttachCardsTo(legolas, bow);
		scn.MoveCardsToHand(defiance1, defiance2, defiance3, defiance4);
		scn.AddWoundsToChar(legolas, 2);
		scn.AddWoundsToChar(haldir, 1);

		scn.StartGame();
		assertAttachedTo(bow, legolas);
		assertInHand(defiance1, defiance2, defiance3, defiance4);
		assertEquals(2, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(haldir));
		scn.MoveMinionsToTable(hound1, hound2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{legolas, hound1}, new PhysicalCardImpl[]{haldir, hound2});
		scn.FreepsResolveSkirmish(legolas);

		assertEquals(2, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(haldir));

		// Declined: nothing is healed.
		scn.FreepsPlayCard(defiance1);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bow));
		scn.FreepsDeclineOptionalTrigger();
		scn.FreepsChooseCard(legolas);
		assertEquals(2, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(haldir));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Taken: one wound is healed from Legolas, and none from Haldir, wounded in a skirmish still to come.
		scn.ShadowPass();
		scn.FreepsPlayCard(defiance2);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bow));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(legolas);
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(haldir));

		// Each time: the next one heals him again.
		scn.ShadowPass();
		scn.FreepsPlayCard(defiance3);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bow));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(legolas);
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(haldir));

		// With no wound on him the heal would do nothing, and it is not offered.
		scn.ShadowPass();
		scn.FreepsPlayCard(defiance4);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(bow));
		scn.FreepsChooseCard(legolas);
		assertInDiscard(defiance1, defiance2, defiance3, defiance4);
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void IsNotOfferedForAnElvenSkirmishEventInAnotherSkirmishOrForAnotherCulturesSkirmishEventInHis() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetFreepsCard("bow");
		var legolas = scn.GetFreepsCard("legolas");
		var haldir = scn.GetFreepsCard("haldir");
		var defiance1 = scn.GetFreepsCard("defiance1");
		var defiance2 = scn.GetFreepsCard("defiance2");
		var swordarm = scn.GetFreepsCard("swordarm");
		var hound1 = scn.GetShadowCard("hound1");
		var hound2 = scn.GetShadowCard("hound2");
		scn.MoveCompanionsToTable(legolas, haldir);
		scn.AttachCardsTo(legolas, bow);
		scn.MoveCardsToHand(defiance1, defiance2, swordarm);
		scn.AddWoundsToChar(legolas, 2);

		scn.StartGame();
		assertAttachedTo(bow, legolas);
		assertInHand(defiance1, defiance2, swordarm);
		assertEquals(2, scn.GetWoundsOn(legolas));
		scn.MoveMinionsToTable(hound1, hound2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{legolas, hound1}, new PhysicalCardImpl[]{haldir, hound2});

		// Haldir's skirmish is fought first: Legolas, wounded, is assigned and waiting for his own.
		scn.FreepsResolveSkirmish(haldir);
		assertEquals(2, scn.GetWoundsOn(legolas));
		scn.FreepsPlayCard(defiance1);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(bow));
		scn.FreepsChooseCard(haldir);
		assertEquals(2, scn.GetWoundsOn(legolas));
		assertInDiscard(defiance1);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Haldir (7) beats his Hound (5).
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(legolas);
		assertTrue(scn.IsCharSkirmishingAgainst(legolas, hound1));

		// In his own skirmish, a skirmish event of another culture.
		scn.FreepsPlayCard(swordarm);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(bow));
		assertInDiscard(swordarm);
		assertEquals(2, scn.GetWoundsOn(legolas));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// The control: an [elven] skirmish event in the same skirmish.
		scn.ShadowPass();
		scn.FreepsPlayCard(defiance2);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bow));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(legolas);
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void IsNotOfferedForAnElvenResponseEventPlayedAsLegolasWinsHisSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetFreepsCard("bow");
		var legolas = scn.GetFreepsCard("legolas");
		var night = scn.GetFreepsCard("night");
		var hound1 = scn.GetShadowCard("hound1");
		scn.MoveCompanionsToTable(legolas);
		scn.AttachCardsTo(legolas, bow);
		scn.MoveCardsToHand(night);
		scn.AddWoundsToChar(legolas, 1);

		scn.StartGame();
		assertAttachedTo(bow, legolas);
		assertInHand(night);
		assertEquals(1, scn.GetWoundsOn(legolas));
		scn.MoveMinionsToTable(hound1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(legolas, hound1);
		scn.FreepsResolveSkirmish(legolas);

		// Legolas (7) beats the Hound (5).
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(hound1));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(night));
		assertEquals(1, scn.GetWoundsOn(legolas));

		// An [elven] event that is no skirmish event, played in his skirmish: he exerts for it and is not healed.
		scn.FreepsChooseOptionalTrigger(night);
		assertEquals(2, scn.GetWoundsOn(legolas));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(bow));
		assertInDiscard(night);
		// The Hound's second wound kills it.
		assertInZone(Zone.DISCARD, hound1);
		assertEquals(2, scn.GetWoundsOn(legolas));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
