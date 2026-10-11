package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.PossessionClass;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_089_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("helm", "5_89");          // Rohirrim Helm
					put("helm2", "5_89");         // a second copy: the card is not unique and says "this possession"
					put("guard", "5_83");         // Household Guard: [rohan] Man, strength 6, vitality 3; Valiant is his whole text
					put("rider", "4_286");        // Rider of Rohan: [rohan] Man, strength 5, vitality 3; his ability needs a mount
					put("guma", "4_277");         // Guma, Plains Farmer: a [rohan] Man who is an ally; a fellowship ability used only on purpose
					// Merry, Master Holbytla: a [rohan] Hobbit, and a Man only beside 3 [rohan] companions or Éowyn
					put("merry", "103_85");
					put("knight", "5_35");        // Gondorian Knight: a Man who is not [rohan]; Knight is his whole text

					put("savage", "1_151");       // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; damage +1 and nothing else
					put("axeman", "4_224");       // Easterling Axeman: [raider] Man, strength 6, vitality 2; his text needs 2 burdens
					put("hound", "7_192");        // Morgul Hound: [wraith] Orc, strength 5, vitality 2; its text needs 2 wounds on the Ring-bearer
					put("runner", "1_178");       // Goblin Runner: strength 5, vitality 1, so it can never exert
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RohirrimHelmStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Rohirrim Helm
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Helm
		 * Game Text: Bearer must be a [rohan] Man.<br><b>Skirmish:</b> Discard this possession to cancel a skirmish involving bearer. A minion in this skirmish may exert to prevent this.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("helm");

		assertEquals("Rohirrim Helm", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HELM));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayedForOneOnARohanManCompanionOrAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var helm = scn.GetFreepsCard("helm");
		var helm2 = scn.GetFreepsCard("helm2");
		var guard = scn.GetFreepsCard("guard");
		var guma = scn.GetFreepsCard("guma");
		var merry = scn.GetFreepsCard("merry");
		var knight = scn.GetFreepsCard("knight");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(guard, merry, knight);
		scn.MoveAlliesToTable(guma);
		scn.MoveCardsToHand(helm, helm2);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(helm));
		scn.FreepsPlayCard(helm);
		// A [rohan] Man, companion or ally; not the [rohan] Hobbit, nor a Man of Gondor.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, guma));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merry, knight, frodo));
		scn.FreepsChooseCard(guma);

		assertAttachedTo(helm, guma);
		assertEquals(1, scn.GetTwilight());

		// The ally has his helm, so the second goes to the one [rohan] Man left without asking.
		scn.FreepsPlayCard(helm2);
		assertAttachedTo(helm2, guard);
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DiscardedToCancelItsBearersSkirmishWhenNoMinionInItCanExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var helm = scn.GetFreepsCard("helm");
		var helm2 = scn.GetFreepsCard("helm2");
		var guard = scn.GetFreepsCard("guard");
		var rider = scn.GetFreepsCard("rider");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(guard, rider);
		scn.AttachCardsTo(guard, helm);
		scn.AttachCardsTo(rider, helm2);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);

		// A skirmish action: not offered in the assignment phase before it.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(helm));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, runner);
		scn.FreepsResolveSkirmish(guard);

		assertTrue(scn.IsCharSkirmishing(guard));
		assertTrue(scn.FreepsActionAvailable(helm));
		scn.FreepsUseCardAction(helm);

		assertInZone(Zone.DISCARD, helm);
		// Vitality 1: the Runner cannot exert, so there is nothing for the Shadow player to be asked.
		assertFalse(scn.ShadowDecisionAvailable("Would you like to exert"));
		// The skirmish is over with no winner: 6 against 5 would have killed the Runner.
		assertFalse(scn.IsCharSkirmishing(guard));
		assertFalse(scn.IsCharSkirmishing(runner));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		// The other Man's Helm is not the one that paid.
		assertAttachedTo(helm2, rider);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AMinionInTheSkirmishMayExertToPreventTheCancel() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var helm = scn.GetFreepsCard("helm");
		var guard = scn.GetFreepsCard("guard");
		var rider = scn.GetFreepsCard("rider");
		var savage = scn.GetShadowCard("savage");
		var axeman = scn.GetShadowCard("axeman");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCompanionsToTable(guard, rider);
		scn.AttachCardsTo(guard, helm);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, axeman, hound);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard, savage}, new PhysicalCardImpl[]{rider, hound});
		scn.ShadowAssignToMinions(guard, axeman);
		scn.FreepsResolveSkirmish(guard);

		scn.FreepsUseCardAction(helm);
		assertInZone(Zone.DISCARD, helm);

		assertTrue(scn.ShadowDecisionAvailable("Would you like to exert"));
		scn.ShadowChooseYes();
		// Either minion skirmishing the bearer, an Uruk-hai or a Man; not the Orc assigned to the other Man.
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, axeman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hound));
		// Having said yes, he has to exert one.
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(axeman);

		assertEquals(1, scn.GetWoundsOn(axeman));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(hound));
		// The Helm is spent all the same, and the skirmish goes on.
		assertInZone(Zone.DISCARD, helm);
		assertTrue(scn.IsCharSkirmishing(guard));
		assertTrue(scn.IsCharSkirmishing(savage));
		assertTrue(scn.IsCharSkirmishing(axeman));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void TheSkirmishIsCancelledWhenTheShadowPlayerDeclinesToExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var helm = scn.GetFreepsCard("helm");
		var guard = scn.GetFreepsCard("guard");
		var savage = scn.GetShadowCard("savage");
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, helm);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, axeman);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, savage);
		scn.ShadowAssignToMinions(guard, axeman);
		scn.FreepsResolveSkirmish(guard);

		assertTrue(scn.IsCharSkirmishing(guard));
		assertTrue(scn.FreepsActionAvailable(helm));
		scn.FreepsUseCardAction(helm);

		assertInZone(Zone.DISCARD, helm);

		assertTrue(scn.ShadowDecisionAvailable("Would you like to exert"));
		scn.ShadowChooseNo();

		// Cancelled: nobody is skirmishing, nobody has exerted, and nobody lost (6 against 11 would have
		// wounded the Guard).
		assertFalse(scn.IsCharSkirmishing(guard));
		assertFalse(scn.IsCharSkirmishing(savage));
		assertFalse(scn.IsCharSkirmishing(axeman));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(axeman));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void UsedInAnotherMansSkirmishItIsDiscardedAndCancelsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var helm = scn.GetFreepsCard("helm");
		var guard = scn.GetFreepsCard("guard");
		var rider = scn.GetFreepsCard("rider");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(guard, rider);
		scn.AttachCardsTo(guard, helm);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(rider, savage);
		scn.FreepsResolveSkirmish(rider);

		// The Guard bears the Helm and is in no skirmish. Discarding it is a cost he can pay.
		assertFalse(scn.IsCharSkirmishing(guard));
		assertTrue(scn.FreepsActionAvailable(helm));
		scn.FreepsUseCardAction(helm);

		assertInZone(Zone.DISCARD, helm);
		// Nothing is going to be cancelled, so the Savage, who could exert, is not asked to prevent it.
		assertFalse(scn.ShadowDecisionAvailable("Would you like to exert"));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.IsCharSkirmishing(rider));
		assertTrue(scn.IsCharSkirmishing(savage));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
