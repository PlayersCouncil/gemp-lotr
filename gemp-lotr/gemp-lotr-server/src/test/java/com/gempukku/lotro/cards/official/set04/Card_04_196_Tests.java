package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_196_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("spear", "4_196");

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, vitality 3; damage +1 and nothing else
					put("guard", "10_81");      // Cirith Ungol Guard: a [sauron] Uruk-hai, vitality 2; damage +1 and nothing else
					put("servant", "3_58");     // Isengard Servant: an [isengard] Orc; a regroup ability only ever used on purpose
					put("saruman", "17_116");   // Saruman, Master of the White Hand: an [uruk-hai] minion who is no Uruk-hai
					put("witchking", "1_237");  // The Witch-king, Lord of Angmar: a Nazgul to bear Black Steed
					put("steed", "1_208");      // Black Steed: a Shadow mount

					put("rider", "4_286");      // Rider of Rohan: [rohan] Man; a skirmish ability only ever used on purpose
					put("ridermount", "4_287"); // Rider's Mount: [rohan] mount; exerts minions only at the start of a skirmish
					put("gandalf", "1_364");    // Gandalf, The Grey Wizard: a fellowship ability only ever used on purpose
					put("shadowfax", "4_100");  // Shadowfax: [gandalf] mount; acts on a hand weapon and at the start of a skirmish
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability only ever used on purpose
					put("sword", "1_112");      // Ranger's Sword: a Free Peoples possession that is no mount
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukSpearStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Uruk Spear
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: +2
		 * Game Text: Bearer must be an Uruk-hai.<br><b>Maneuver:</b> Exert bearer to discard a Free Peoples mount.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("spear");

		assertEquals("Uruk Spear", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnAnUrukHaiOfEitherCultureAndOnNoOtherMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spear = scn.GetShadowCard("spear");
		var savage = scn.GetShadowCard("savage");
		var guard = scn.GetShadowCard("guard");
		var servant = scn.GetShadowCard("servant");
		var saruman = scn.GetShadowCard("saruman");
		scn.MoveCardsToHand(spear);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, guard, servant, saruman);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(spear));
		scn.ShadowPlayCard(spear);
		// Uruk-hai of [isengard] and [sauron]; not an [isengard] Orc, nor an [uruk-hai] Wizard.
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, guard));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(servant, saruman));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(guard);

		assertAttachedTo(spear, guard);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ManeuverAbilityExertsBearerToDiscardAFreePeoplesMount() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spear = scn.GetShadowCard("spear");
		var savage = scn.GetShadowCard("savage");
		var guard = scn.GetShadowCard("guard");
		var witchking = scn.GetShadowCard("witchking");
		var steed = scn.GetShadowCard("steed");
		var rider = scn.GetFreepsCard("rider");
		var ridermount = scn.GetFreepsCard("ridermount");
		var gandalf = scn.GetFreepsCard("gandalf");
		var shadowfax = scn.GetFreepsCard("shadowfax");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(rider, gandalf, aragorn);
		scn.AttachCardsTo(rider, ridermount);
		scn.AttachCardsTo(gandalf, shadowfax);
		scn.AttachCardsTo(aragorn, sword);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, guard, witchking);
		scn.AttachCardsTo(savage, spear);
		scn.AttachCardsTo(witchking, steed);
		assertAttachedTo(spear, savage);
		assertAttachedTo(ridermount, rider);
		assertAttachedTo(shadowfax, gandalf);
		assertAttachedTo(sword, aragorn);
		assertAttachedTo(steed, witchking);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(guard));

		assertTrue(scn.ShadowActionAvailable(spear));
		scn.ShadowUseCardAction(spear);
		// The bearer exerts, not the other Uruk-hai.
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(guard));

		// Free Peoples mounts of two cultures; not a Shadow mount, nor a Free Peoples possession that is no mount.
		assertTrue(scn.ShadowHasCardChoiceAvailable(ridermount, shadowfax));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(steed, sword, spear));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(shadowfax);

		assertInDiscard(shadowfax);
		assertAttachedTo(ridermount, rider);
		assertAttachedTo(steed, witchking);
		assertAttachedTo(sword, aragorn);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ManeuverAbilityIsNotOfferedWhenBearerIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spear = scn.GetShadowCard("spear");
		var savage = scn.GetShadowCard("savage");
		var guard = scn.GetShadowCard("guard");
		var rider = scn.GetFreepsCard("rider");
		var ridermount = scn.GetFreepsCard("ridermount");
		scn.MoveCompanionsToTable(rider);
		scn.AttachCardsTo(rider, ridermount);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, guard);
		scn.AttachCardsTo(savage, spear);
		scn.AddWoundsToChar(savage, 2);
		assertEquals(2, scn.GetWoundsOn(savage));
		assertAttachedTo(spear, savage);
		assertAttachedTo(ridermount, rider);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// Vitality 3 with 2 wounds: the bearer is exhausted.  The unwounded Uruk-hai beside him does not bear it.
		assertEquals(2, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertFalse(scn.ShadowActionAvailable(spear));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertAttachedTo(ridermount, rider);
	}
}
