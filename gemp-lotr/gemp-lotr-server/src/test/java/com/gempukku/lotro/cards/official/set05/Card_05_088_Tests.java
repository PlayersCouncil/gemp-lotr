package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_088_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bow", "5_88");
					put("bow2", "5_88");        // a second copy: the card is not unique and says "this possession"

					put("guard", "5_83");       // Household Guard: [rohan] Man companion, vitality 3; Valiant is his whole text
					put("guma", "4_277");       // Guma, Plains Farmer: [rohan] Man ally, vitality 2; a fellowship ability used only on purpose
					// Merry, Master Holbytla: the one [rohan] character who is no Man.  He is a Man as well only while
					// 3 [rohan] companions (or Eowyn) can be spotted.
					put("merry", "103_85");
					put("knight", "5_35");      // Gondorian Knight: a Man who is not [rohan]; Knight is his whole text

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, vitality 3, damage +1 and nothing else
					put("axeman", "4_224");     // Easterling Axeman: [raider] Man, vitality 2; his text needs 2 burdens
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RohirrimBowStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Rohirrim Bow
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Ranged Weapon
		 * Strength: +1
		 * Game Text: Bearer must be a [rohan] Man.
		 * 	<b>Archery:</b> Exert bearer and discard this possession to wound a minion.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("bow");

		assertEquals("Rohirrim Bow", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.RANGED_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnARohanManCompanionOrAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetFreepsCard("bow");
		var guard = scn.GetFreepsCard("guard");
		var guma = scn.GetFreepsCard("guma");
		var merry = scn.GetFreepsCard("merry");
		var knight = scn.GetFreepsCard("knight");
		var frodo = scn.GetRingBearer();
		// Two [rohan] companions, the Guard and Merry: Merry is no Man.
		scn.MoveCompanionsToTable(guard, merry, knight);
		scn.MoveAlliesToTable(guma);
		scn.MoveCardsToHand(bow);

		scn.StartGame();

		assertTrue(scn.IsRace(merry, Race.HOBBIT));
		assertFalse(scn.IsRace(merry, Race.MAN));
		assertInHand(bow);
		assertTrue(scn.FreepsPlayAvailable(bow));
		scn.FreepsPlayCard(bow);

		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, guma));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merry, knight, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(guma);

		assertAttachedTo(bow, guma);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ArcheryAbilityExertsItsBearerAndDiscardsItToWoundAMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetFreepsCard("bow");
		var bow2 = scn.GetFreepsCard("bow2");
		var guard = scn.GetFreepsCard("guard");
		var guma = scn.GetFreepsCard("guma");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(guard);
		scn.MoveAlliesToTable(guma);
		scn.AttachCardsTo(guard, bow);
		scn.AttachCardsTo(guma, bow2);

		var savage = scn.GetShadowCard("savage");
		var axeman = scn.GetShadowCard("axeman");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, axeman);
		scn.SkipToPhase(Phase.ARCHERY);

		assertAttachedTo(bow, guard);
		assertAttachedTo(bow2, guma);
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(guma));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		// The Bow an ally bears: he is the bearer who exerts.
		assertTrue(scn.FreepsActionAvailable(bow2));
		scn.FreepsUseCardAction(bow2);

		assertEquals(1, scn.GetWoundsOn(guma));
		assertInDiscard(bow2);
		// The other copy and its bearer are untouched.
		assertEquals(0, scn.GetWoundsOn(guard));
		assertAttachedTo(bow, guard);

		// A minion of either culture and race; no character who is no minion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, axeman));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(guard, guma, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(axeman);

		assertEquals(1, scn.GetWoundsOn(axeman));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void ArcheryAbilityIsNotOfferedWhileItsBearerIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetFreepsCard("bow");
		var bow2 = scn.GetFreepsCard("bow2");
		var guard = scn.GetFreepsCard("guard");
		var guma = scn.GetFreepsCard("guma");
		scn.MoveCompanionsToTable(guard);
		scn.MoveAlliesToTable(guma);
		scn.AttachCardsTo(guard, bow);
		scn.AttachCardsTo(guma, bow2);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.AddWoundsToChar(guard, 2);
		assertEquals(2, scn.GetWoundsOn(guard));
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.ARCHERY);

		// Vitality 3 with 2 wounds: the Guard cannot exert.  Guma, who can, is offered his Bow.
		assertAttachedTo(bow, guard);
		assertAttachedTo(bow2, guma);
		assertEquals(2, scn.GetWoundsOn(guard));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsActionAvailable(bow));
		assertTrue(scn.FreepsActionAvailable(bow2));
	}
}
