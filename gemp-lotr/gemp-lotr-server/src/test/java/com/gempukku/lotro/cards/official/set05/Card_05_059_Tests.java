package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_059_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("warg", "5_59");
					put("sharku", "6_74");      // Sharku, Vile Marauder: warg-rider, strength 7, vitality 2; fierce while not exhausted; a regroup ability
					put("sharku2", "5_58");     // Sharku, Warg-captain: warg-rider; answers the PLAY of an [isengard] mount, so only cheated in
					put("rider", "5_67");       // Warg-rider: warg-rider, strength 5, vitality 2; fierce while not exhausted
					put("shaman", "3_59");      // Isengard Shaman: an [isengard] Orc who is no warg-rider, strength 5, vitality 2;
					                            // Response: if an [isengard] Orc is about to take a wound, remove (2) to prevent it

					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3; Skirmish: exert Gimli to make him strength +2
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("axestrike", "1_3");    // Axe Strike: Skirmish event, a Dwarf strength +2 (+3 with a [dwarven] hand weapon)
					put("slayer", "1_153");     // Uruk Slayer: an [isengard] Uruk-hai, strength 9, damage +1; Skirmish: remove (1) to make himself strength +1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SharkusWargStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Sharku's Warg
		 * Unique: True
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 5
		 * Type: Possession
		 * Subtype: Mount
		 * Strength: +5
		 * Vitality: +3
		 * Game Text: Bearer must be a warg-rider.<br>If bearer is Sharku, he is <b>damage +1</b>.<br><b>Response:</b> If a skirmish special ability is used in a skirmish involving bearer, exert bearer to cancel that action.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("warg");

		assertEquals("Sharku's Warg", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.MOUNT));
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
	}

	@Test
	public void PlaysOnAWargRiderAndMakesSharkuDamagePlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warg = scn.GetShadowCard("warg");
		var sharku = scn.GetShadowCard("sharku");
		var rider = scn.GetShadowCard("rider");
		var shaman = scn.GetShadowCard("shaman");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToHand(warg);

		scn.StartGame();
		scn.SetTwilight(5);
		// Laid before the Shadow phase begins, so that its first choice of actions takes them in.
		scn.MoveMinionsToTable(sharku, rider, shaman);
		scn.FreepsPass();

		assertEquals(0, scn.GetKeywordCount(sharku, Keyword.DAMAGE));
		assertTrue(scn.ShadowPlayAvailable(warg));
		scn.ShadowPlayCard(warg);
		// Two warg-riders, and an [isengard] Orc who is none.
		assertTrue(scn.ShadowHasCardChoiceAvailable(sharku, rider));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(shaman, frodo));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(sharku);

		assertAttachedTo(warg, sharku);
		assertEquals(1, scn.GetKeywordCount(sharku, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(rider, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void BorneByAnotherWargRiderItMakesNobodyDamagePlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warg = scn.GetShadowCard("warg");
		var sharku = scn.GetShadowCard("sharku2");
		var rider = scn.GetShadowCard("rider");

		scn.StartGame();
		scn.MoveMinionsToTable(sharku, rider);
		assertEquals(0, scn.GetKeywordCount(sharku, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(rider, Keyword.DAMAGE));

		scn.AttachCardsTo(rider, warg);
		assertAttachedTo(warg, rider);
		// The bearer is no Sharku, and the Sharku in play is no bearer.
		assertEquals(0, scn.GetKeywordCount(rider, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(sharku, Keyword.DAMAGE));
	}

	@Test
	public void ExertsBearerToCancelASkirmishSpecialAbilityUsedInHisSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warg = scn.GetShadowCard("warg");
		var rider = scn.GetShadowCard("rider");
		var sharku = scn.GetShadowCard("sharku");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(rider, sharku);
		scn.AttachCardsTo(rider, warg);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, rider);
		// Sharku fights nobody.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gimli);

		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(rider));
		assertEquals(0, scn.GetWoundsOn(sharku));
		assertEquals(6, scn.GetStrength(gimli));

		scn.FreepsUseCardAction(gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(warg));
		scn.ShadowAcceptOptionalTrigger();

		// The bearer pays, and the Sharku beside him does not.
		assertEquals(1, scn.GetWoundsOn(rider));
		assertEquals(0, scn.GetWoundsOn(sharku));
		// Cancelled: Gimli's exertion stays paid and he is not strength +2.
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(6, scn.GetStrength(gimli));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AnswersASitesSkirmishAbilityButNotAResponseAbilityUsedInItsBearersSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warg = scn.GetShadowCard("warg");
		var rider = scn.GetShadowCard("rider");
		var shaman = scn.GetShadowCard("shaman");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		// Aragorn is given the strength to beat the Warg-rider: 8 + 5 against 5 + 5 for the Warg.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 5));
		scn.MoveMinionsToTable(rider, shaman);
		scn.AttachCardsTo(rider, warg);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, rider);
		// The Shaman fights nobody.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(2);

		assertEquals(13, scn.GetStrength(aragorn));
		assertEquals(10, scn.GetStrength(rider));

		// Ettenmoors, site 2: "Skirmish: Exert your companion or minion to make that character strength +2."
		// A site's skirmish ability, used by no character, in the bearer's skirmish.
		var ettenmoors = scn.GetCurrentSite();
		scn.FreepsUseCardAction(ettenmoors);
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(warg));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(15, scn.GetStrength(aragorn));

		// Aragorn wins, and the Warg-rider, an [isengard] Orc, is about to take a wound.
		scn.ShadowPass();
		scn.FreepsPass();
		assertTrue(scn.IsCharSkirmishing(rider));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(shaman));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(0, scn.GetTwilight());

		// The Shaman's Response is used during that skirmish, and it is no skirmish special ability: nothing is
		// offered, and the wound is prevented.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(warg));
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertEquals(0, scn.GetWoundsOn(rider));
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void IsNotOfferedForASkirmishEventInItsBearersSkirmishNorForASkirmishAbilityInAnother() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warg = scn.GetShadowCard("warg");
		var rider = scn.GetShadowCard("rider");
		var shaman = scn.GetShadowCard("shaman");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var axestrike = scn.GetFreepsCard("axestrike");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveCardsToHand(axestrike);

		scn.StartGame();
		scn.MoveMinionsToTable(rider, shaman);
		scn.AttachCardsTo(rider, warg);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, rider}, new PhysicalCardImpl[]{aragorn, shaman});

		// Another skirmish is fought first, with the bearer assigned and waiting for his own:
		// Ettenmoors' skirmish ability is used where the bearer is not.
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetWoundsOn(rider));
		assertEquals(8, scn.GetStrength(aragorn));
		var ettenmoors = scn.GetCurrentSite();
		scn.FreepsUseCardAction(ettenmoors);
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(warg));
		assertEquals(10, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetWoundsOn(rider));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// 10 against 5: the Shaman is overwhelmed, and the bearer's skirmish is the one left.
		scn.ShadowPass();
		scn.FreepsPass();
		assertInZone(Zone.DISCARD, shaman);

		// The bearer's skirmish: a skirmish event, which is no special ability.
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(6, scn.GetStrength(gimli));
		scn.FreepsPlayCard(axestrike);
		assertEquals(8, scn.GetStrength(gimli));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(warg));
		assertEquals(0, scn.GetWoundsOn(rider));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AnswersASkirmishSpecialAbilityTheShadowPlayerUsesInItsBearersSkirmishToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warg = scn.GetShadowCard("warg");
		var rider = scn.GetShadowCard("rider");
		var slayer = scn.GetShadowCard("slayer");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(rider, slayer);
		scn.AttachCardsTo(rider, warg);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, rider);
		// The Slayer fights nobody.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gimli);
		scn.SetTwilight(1);
		scn.FreepsPass();

		assertEquals(9, scn.GetStrength(slayer));
		assertEquals(0, scn.GetWoundsOn(rider));
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(slayer));

		scn.ShadowUseCardAction(slayer);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(warg));
		scn.ShadowAcceptOptionalTrigger();

		// Cancelled: the Slayer's price stays paid and he is not strength +1.
		assertEquals(1, scn.GetWoundsOn(rider));
		assertEquals(0, scn.GetWoundsOn(slayer));
		assertEquals(9, scn.GetStrength(slayer));
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void IsNotOfferedWhileItsBearerIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warg = scn.GetShadowCard("warg");
		var rider = scn.GetShadowCard("rider");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(rider);
		scn.AttachCardsTo(rider, warg);
		// Vitality 2 + 3 for the Warg, with 4 wounds: the bearer cannot exert.
		scn.AddWoundsToChar(rider, 4);
		assertEquals(4, scn.GetWoundsOn(rider));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, rider);
		scn.FreepsResolveSkirmish(gimli);

		assertEquals(4, scn.GetWoundsOn(rider));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(6, scn.GetStrength(gimli));

		scn.FreepsUseCardAction(gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(warg));
		assertEquals(4, scn.GetWoundsOn(rider));
		assertEquals(8, scn.GetStrength(gimli));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
