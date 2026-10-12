package com.gempukku.lotro.mechanics.actions;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * A Response special ability that is used is a special ability used.
 *
 * Comprehensive Rules 4.2, "response": "A special ability or event labeled with the word 'Response:' indicates
 * that you may perform that action whenever the trigger described in its game text happens.  You may respond more
 * than once to the same situation.  A response action is not a phase action (because there is no 'response
 * phase')."  Ketura on Q-0039, what a special ability is: "It would count, if it's got the bold text and a colon."
 * And on Q-0119, of "a skirmish event or special ability is used during a skirmish" (Boromir's Cloak 51_98):
 * "I *meant* 'skirmish event or skirmish special ability'.  However what I *wrote* does permit for Response
 * actions as well."
 *
 * So a card that answers "a special ability is used" answers a Response ability, and a card that answers "a
 * skirmish special ability" (or a regroup one) does not, in whatever phase the Response ability is used: its
 * boldfaced word is Response.
 *
 * F-0201 supposed that the game could do neither: that a Response ability was an optional trigger which never
 * reported itself as used.  It is not.  A definition's `type: Response` is an ActivateCardAction as a phase
 * ability is (ResponseEffectProcessor, offered by ActivateResponseAbilitiesRule), with the timeword Response, and
 * when its costs are paid it publishes the same ActivateCardResult.  The UsesSpecialAbility trigger reads that
 * result: with no `phase` it answers every special ability, and with one it answers only the abilities labelled
 * with that word.  These tests hold both readings in place.
 *
 * The cards: Storied Homestead 13_76 ("Response: If a minion's special ability is used, discard this condition or
 * remove 2 [gondor] tokens from here to cancel its effect") names no phase; War-warg 5_64 ("Response: If a
 * skirmish special ability is used in a skirmish involving bearer, exert bearer to cancel that action") names
 * one; Isengard Shaman 3_59 ("Response: If an [isengard] Orc is about to take a wound, remove (2) to prevent that
 * wound") is the Response ability used.
 */
public class ResponseAbilityIsASpecialAbilityTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("homestead", "13_76");  // Storied Homestead: [gondor] condition in the support area
					put("shaman", "3_59");      // Isengard Shaman: [isengard] Orc, strength 5, vitality 2
					put("rider", "5_67");       // Warg-rider: [isengard] Orc, strength 5, vitality 2; fierce while not exhausted
					put("warg", "5_64");        // War-warg: [isengard] mount on a warg-rider, strength +4, vitality +2

					put("legolas", "1_50");     // Legolas, Greenleaf: archer; an archery ability that is never used here
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8; a maneuver ability that is never used here
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AResponseAbilityUsedIsAnsweredByACardThatAnswersASpecialAbility() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var homestead = scn.GetFreepsCard("homestead");
		var legolas = scn.GetFreepsCard("legolas");
		var shaman = scn.GetShadowCard("shaman");
		scn.MoveCardsToSupportArea(homestead);
		scn.AddTokensToCard(homestead, 2);
		scn.MoveCompanionsToTable(legolas);

		scn.StartGame();
		scn.MoveMinionsToTable(shaman);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.SetTwilight(2);
		scn.PassCurrentPhaseActions();

		// One archery wound, and the Shaman is the only minion to take it: he is about to.
		assertEquals(0, scn.GetWoundsOn(shaman));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(shaman));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(0, scn.GetTwilight());

		// A minion's special ability has been used.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(homestead));
		scn.FreepsDeclineOptionalTrigger();

		// Left unanswered, the Shaman's Response prevents the wound.
		assertEquals(0, scn.GetWoundsOn(shaman));
		assertEquals(2, scn.GetCultureTokensOn(homestead));
		assertInZone(Zone.SUPPORT, homestead);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AResponseAbilityThatIsCancelledKeepsItsCostPaidAndEachFurtherUseIsAnsweredAfresh() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var homestead = scn.GetFreepsCard("homestead");
		var legolas = scn.GetFreepsCard("legolas");
		var shaman = scn.GetShadowCard("shaman");
		scn.MoveCardsToSupportArea(homestead);
		scn.AddTokensToCard(homestead, 4);
		scn.MoveCompanionsToTable(legolas);

		scn.StartGame();
		scn.MoveMinionsToTable(shaman);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.SetTwilight(4);
		scn.PassCurrentPhaseActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(shaman));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(2, scn.GetTwilight());

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(homestead));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChoose("Remove 2");
		assertEquals(2, scn.GetCultureTokensOn(homestead));

		// The effect of that use is cancelled: the (2) stays removed and the wound is still about to be taken.
		assertEquals(2, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(shaman));
		// "You may respond more than once to the same situation": the Shaman is offered again, and uses his
		// Response a second time.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(shaman));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(0, scn.GetTwilight());

		// A second use, answered as the first was.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(homestead));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChoose("Remove 2");
		assertEquals(0, scn.GetCultureTokensOn(homestead));
		assertInZone(Zone.SUPPORT, homestead);

		// With nothing left to pay for a third use, the wound lands.
		assertEquals(1, scn.GetWoundsOn(shaman));
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AResponseAbilityUsedInASkirmishIsNotASkirmishSpecialAbility() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var homestead = scn.GetFreepsCard("homestead");
		var aragorn = scn.GetFreepsCard("aragorn");
		var rider = scn.GetShadowCard("rider");
		var warg = scn.GetShadowCard("warg");
		var shaman = scn.GetShadowCard("shaman");
		scn.MoveCardsToSupportArea(homestead);
		scn.AddTokensToCard(homestead, 2);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(rider, shaman);
		scn.AttachCardsTo(rider, warg);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, rider);
		// The Shaman fights nobody.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(2);

		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(9, scn.GetStrength(rider));

		// Ettenmoors, site 2: "Skirmish: Exert your companion or minion to make that character strength +2."
		// A skirmish special ability used in the skirmish of the War-warg's bearer: the War-warg answers it.
		// It is a site's ability and no minion's, so the Homestead does not.
		var ettenmoors = scn.GetCurrentSite();
		scn.FreepsUseCardAction(ettenmoors);
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(homestead));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(warg));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(10, scn.GetStrength(aragorn));

		// Aragorn wins, 10 to 9, and the Warg-rider, an [isengard] Orc, is about to take a wound.
		scn.ShadowPass();
		scn.FreepsPass();
		assertTrue(scn.IsCharSkirmishing(rider));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(shaman));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(0, scn.GetTwilight());

		// The Shaman's Response is used during that skirmish.  It is a special ability, and a minion's: the
		// Homestead answers it.
		assertTrue(scn.IsCharSkirmishing(rider));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(homestead));
		scn.FreepsDeclineOptionalTrigger();

		// It is no skirmish special ability: the Shadow player, whose turn to answer it now is, is asked nothing,
		// and the game goes on.  The wound is prevented and the fierce Warg-rider is to be assigned again.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(warg));
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertEquals(0, scn.GetWoundsOn(rider));
		assertFalse(scn.IsCharSkirmishing(rider));
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
