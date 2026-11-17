package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_147_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("cover", "4_147");
					put("cover2", "4_147");

					put("assassin", "1_131");   // Orthanc Assassin: unique [isengard] Uruk-hai archer, vitality 3; damage +1 and nothing else
					put("crossbow", "4_182");   // Uruk Crossbow Troop: [isengard] Uruk-hai archer, cost 4; "When you play this minion, you may place an [isengard] token on a machine" (no machine here)
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, no archer; damage +1 and nothing else
					// Lurtz, Halfling Hunter: unique [uruk-hai] Uruk-hai archer (the keyword without the culture); a maneuver and
					// an assignment ability used only on purpose
					put("lurtz", "18_118");
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					// Wreathed in Shadow: [wraith] support-area condition; "Archery: Remove (1) to make the fellowship archery
					// total -1"; its "To play, spot a Nazgul" is passed over by the cheat
					put("wreathed", "1_238");
					// Driven Back: [isengard] archery event, cost 3; "Spot 2 [isengard] archers to make the minion archery total
					// +1 for each card in the dead pile"
					put("drivenback", "4_149");
					// Weapons of Isengard: [isengard] machine condition in the support area; its abilities are used only on purpose
					put("weapons", "4_211");

					put("legolas", "1_50");     // Legolas, Greenleaf: Elf companion and archer; "Archery: Exert Legolas to wound a minion"
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion, vitality 3; a skirmish ability used only on purpose
					put("axe", "2_10");         // Hand Axe: a Dwarf's hand weapon; "Archery: Discard this weapon to make the fellowship archery total +1"
					put("bracers", "2_3");      // Dwarven Bracers: a Dwarf's possession; "Response: If bearer is about to take a wound, discard this possession to prevent that wound"
					put("lightfoot", "1_52");   // Lightfootedness: [elven] archery event, cost 0; "Spot an Elf companion to make the minion archery total -1"
					// Gondor Will See It Done: [gondor] Response event, cost 0; "If a companion is about to take a wound, exert a
					// [gondor] companion to prevent that wound"
					put("gondorwill", "2_34");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, strength 8, vitality 4; a maneuver ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CoveringFireStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Covering Fire
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, spot 2 [isengard] archers. Plays to your support area.<br>The Free Peoples player may not play archery events or use archery special abilities. Discard this condition during the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("cover");

		assertEquals("Covering Fire", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedWithOneIsengardArcherButCanOnceASecondIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cover = scn.GetShadowCard("cover");
		var assassin = scn.GetShadowCard("assassin");
		var crossbow = scn.GetShadowCard("crossbow");
		var savage = scn.GetShadowCard("savage");
		var lurtz = scn.GetShadowCard("lurtz");
		scn.MoveCardsToHand(cover, crossbow);

		scn.StartGame();
		assertInHand(cover, crossbow);
		scn.MoveMinionsToTable(assassin, savage, lurtz);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// One [isengard] archer; the Savage is [isengard] and no archer, Lurtz an archer of [uruk-hai].
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(cover));

		// A second [isengard] archer, played from hand.
		scn.ShadowPlayCard(crossbow);
		assertInZone(Zone.SHADOW_CHARACTERS, crossbow);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(cover));
		scn.ShadowPlayCard(cover);
		assertInZone(Zone.SUPPORT, cover);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheFreePeoplesPlayerCanPlayNoArcheryEventAndUseNoArcherySpecialAbilityWhileItIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");
		var lightfoot = scn.GetFreepsCard("lightfoot");
		scn.MoveCompanionsToTable(legolas, gimli);
		scn.AttachCardsTo(gimli, axe);
		scn.MoveCardsToHand(lightfoot);

		var cover = scn.GetShadowCard("cover");
		var assassin = scn.GetShadowCard("assassin");
		var crossbow = scn.GetShadowCard("crossbow");
		var wreathed = scn.GetShadowCard("wreathed");
		var drivenback = scn.GetShadowCard("drivenback");
		scn.MoveCardsToSupportArea(wreathed);
		scn.MoveCardsToHand(drivenback);

		scn.StartGame();
		assertAttachedTo(axe, gimli);
		assertInHand(lightfoot, drivenback);
		assertInZone(Zone.SUPPORT, wreathed);
		scn.MoveMinionsToTable(assassin, crossbow);
		scn.SkipToPhase(Phase.ARCHERY);

		// Without Covering Fire: an archery event to play, and the archery special abilities of a companion and of a
		// possession.
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(lightfoot));
		assertTrue(scn.FreepsActionAvailable(legolas));
		assertTrue(scn.FreepsActionAvailable(axe));

		// With it in the Shadow player's support area, none of them.
		scn.MoveCardsToSupportArea(cover);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.SUPPORT, cover);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(lightfoot));
		assertFalse(scn.FreepsActionAvailable(legolas));
		assertFalse(scn.FreepsActionAvailable(axe));
		scn.SetTwilight(5);
		scn.FreepsPass();

		// The Shadow player's own archery event and archery special ability are not barred.
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(drivenback));
		assertTrue(scn.ShadowActionAvailable(wreathed));
	}

	@Test
	public void AResponseEventAndAResponseAbilityAreNoArcheryOnesAndAreStillUsedInTheArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var bracers = scn.GetFreepsCard("bracers");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gondorwill = scn.GetFreepsCard("gondorwill");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.AttachCardsTo(gimli, bracers);
		scn.MoveCardsToHand(gondorwill);

		var cover = scn.GetShadowCard("cover");
		var assassin = scn.GetShadowCard("assassin");
		var crossbow = scn.GetShadowCard("crossbow");
		scn.MoveCardsToSupportArea(cover);

		scn.StartGame();
		assertAttachedTo(bracers, gimli);
		assertInHand(gondorwill);
		assertInZone(Zone.SUPPORT, cover);
		scn.MoveMinionsToTable(assassin, crossbow);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.PassArcheryActions();

		// Minion archery total 2.  The first wound goes to Gimli: the Response event is offered beside the Bracers, and
		// the [gondor] companion exerts to prevent it.
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.FreepsChooseCard(gimli);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gondorwill));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bracers));
		scn.FreepsChooseOptionalTrigger(gondorwill);
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertInDiscard(gondorwill);

		// The second wound goes to Gimli too, and his Bracers prevent it.
		scn.FreepsChooseCard(gimli);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bracers));
		scn.FreepsChooseOptionalTrigger(bracers);
		assertInDiscard(bracers);
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void StaysThroughTheSkirmishWhereASkirmishAbilityIsStillUsableAndIsDiscardedAsTheRegroupPhaseStarts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var cover = scn.GetShadowCard("cover");
		var cover2 = scn.GetShadowCard("cover2");
		var weapons = scn.GetShadowCard("weapons");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(cover, cover2, weapons);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, cover, cover2, weapons);
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);

		// In the skirmish, the phase before regroup: both copies still in play.  The bar is on archery: a skirmish
		// special ability of the Free Peoples player is on offer.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertInZone(Zone.SUPPORT, cover, cover2);
		assertTrue(scn.FreepsActionAvailable(gimli));
		scn.PassSkirmishActions();

		// The Troop (13) beats Aragorn (8).  As the regroup phase starts each copy is discarded; the other Shadow
		// support condition stays.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertInDiscard(cover, cover2);
		assertInZone(Zone.SUPPORT, weapons);
	}
}
