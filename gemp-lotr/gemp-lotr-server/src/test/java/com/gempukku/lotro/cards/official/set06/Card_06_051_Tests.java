package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.VitalityModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_051_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("banner", "6_51");
					put("banner2", "6_51");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, strength 8, vitality 4; a maneuver ability used only on purpose
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion, strength 6, vitality 3; Knight only
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] companion who is a Hobbit, vitality 4; his skirmish ability used only on purpose
					put("merchant", "7_97");    // Gondorian Merchant: the [gondor] ally, a Man, vitality 1; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: a companion of another culture, vitality 3; a skirmish ability used only on purpose
					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3; Damage +1 only
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, vitality 3; no text
					// Uruk Seeker: fierce, strength 6, vitality 2; its other text is only against a bearer of a search card
					put("seeker", "4_195");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BannerofWesternesseStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Banner of Westernesse
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Game Text: Bearer must be a [gondor] companion.<br>Each time bearer wins a skirmish, you may heal another [gondor] companion.<br><b>Response:</b> If bearer wins a skirmish, discard this possession to liberate a site.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("banner");

		assertEquals("Banner of Westernesse", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void BearerMustBeAGondorCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var banner = scn.GetFreepsCard("banner");
		var aragorn = scn.GetFreepsCard("aragorn");
		var pippin = scn.GetFreepsCard("pippin");
		var merchant = scn.GetFreepsCard("merchant");
		var gimli = scn.GetFreepsCard("gimli");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn, pippin, gimli);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToHand(banner);

		scn.StartGame();

		assertInHand(banner);
		assertTrue(scn.FreepsPlayAvailable(banner));
		scn.FreepsPlayCard(banner);

		// Two [gondor] companions, a Man and a Hobbit; not the [gondor] ally, nor companions of other cultures.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merchant, gimli, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(pippin);

		assertAttachedTo(banner, pippin);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachTimeBearerWinsASkirmishHeMayHealAnotherGondorCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var banner = scn.GetFreepsCard("banner");
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		var pippin = scn.GetFreepsCard("pippin");
		var merchant = scn.GetFreepsCard("merchant");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, knight, pippin, gimli);
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(aragorn, banner);
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		assertAttachedTo(banner, aragorn);
		// A vitality that only arranges the board: the Merchant lives through the wound laid on him.
		scn.ApplyAdHocModifier(new VitalityModifier(null, merchant, null, 1));
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddWoundsToChar(knight, 2);
		scn.AddWoundsToChar(pippin, 1);
		scn.AddWoundsToChar(gimli, 1);
		scn.AddWoundsToChar(merchant, 1);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(knight));
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(merchant));

		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.FreepsResolveSkirmish(aragorn);

		// 8 against 5: Aragorn wins.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(banner, "Heal"));
		scn.FreepsChooseOptionalTrigger(banner, "Heal");

		// Two wounded [gondor] companions other than the bearer; not the bearer, the [gondor] ally, nor Gimli.
		assertTrue(scn.FreepsHasCardChoiceAvailable(knight, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, merchant, gimli));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(knight);

		// One wound healed of two.
		assertEquals(1, scn.GetWoundsOn(knight));
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(merchant));
		assertAttachedTo(banner, aragorn);
		// The Response to the same win is still on offer.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(banner, "liberate"));
	}

	@Test
	public void DecliningTheHealHealsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var banner = scn.GetFreepsCard("banner");
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(aragorn, knight);
		scn.AttachCardsTo(aragorn, banner);
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		assertAttachedTo(banner, aragorn);
		scn.AddWoundsToChar(knight, 2);
		assertEquals(2, scn.GetWoundsOn(knight));
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(banner, "Heal"));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(2, scn.GetWoundsOn(knight));
		assertAttachedTo(banner, aragorn);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NeitherIsOfferedWhenBearerLosesASkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var banner = scn.GetFreepsCard("banner");
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(aragorn, knight);
		scn.AttachCardsTo(aragorn, banner);
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		assertAttachedTo(banner, aragorn);
		// A wounded [gondor] companion, so that the heal would have something to act on.
		scn.AddWoundsToChar(knight, 1);
		assertEquals(1, scn.GetWoundsOn(knight));
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);

		// 13 against 8, not doubled: Aragorn loses and lives through one wound.
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(banner));
		assertEquals(1, scn.GetWoundsOn(knight));
		assertAttachedTo(banner, aragorn);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NeitherIsOfferedWhenAGondorCompanionWhoIsNotBearerWins() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var banner = scn.GetFreepsCard("banner");
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(aragorn, knight, pippin);
		scn.AttachCardsTo(aragorn, banner);
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		assertAttachedTo(banner, aragorn);
		scn.AddWoundsToChar(pippin, 1);
		assertEquals(1, scn.GetWoundsOn(pippin));
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, savage);
		scn.FreepsResolveSkirmish(knight);

		// 6 against 5: the Knight, a [gondor] companion the Banner could go on, wins without bearing it.
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(savage));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(banner));
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertAttachedTo(banner, aragorn);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoOtherGondorCompanionWoundedOnlyTheResponseIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var banner = scn.GetFreepsCard("banner");
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, knight, gimli);
		scn.AttachCardsTo(aragorn, banner);
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		assertAttachedTo(banner, aragorn);
		// The bearer and a companion of another culture are wounded; the other [gondor] companion is not.
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddWoundsToChar(gimli, 1);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(knight));
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		assertFalse(scn.FreepsHasOptionalTriggerAvailable(banner, "Heal"));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(banner, "liberate"));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertAttachedTo(banner, aragorn);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void BearerWinningTheFierceSkirmishAsWellMayHealAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var banner = scn.GetFreepsCard("banner");
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(aragorn, knight);
		scn.AttachCardsTo(aragorn, banner);
		var seeker = scn.GetShadowCard("seeker");

		scn.StartGame();
		assertAttachedTo(banner, aragorn);
		scn.AddWoundsToChar(knight, 2);
		assertEquals(2, scn.GetWoundsOn(knight));
		scn.MoveMinionsToTable(seeker);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, seeker);
		scn.FreepsResolveSkirmish(aragorn);

		// 8 against 6: Aragorn wins; the Knight is the one candidate and is healed unasked.
		scn.PassSkirmishActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(banner, "Heal"));
		scn.FreepsChooseOptionalTrigger(banner, "Heal");
		assertEquals(1, scn.GetWoundsOn(knight));
		// The Response is declined, and the Banner stays.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(banner, "liberate"));
		scn.FreepsDeclineOptionalTrigger();
		assertAttachedTo(banner, aragorn);
		assertEquals(1, scn.GetWoundsOn(seeker));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, seeker);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassFierceSkirmishActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(banner, "Heal"));
		scn.FreepsChooseOptionalTrigger(banner, "Heal");
		assertEquals(0, scn.GetWoundsOn(knight));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(banner, "liberate"));
	}

	@Test
	public void ResponseDiscardsTheBannerToLiberateTheHighestSiteAnOpponentControls() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var banner = scn.GetFreepsCard("banner");
		var banner2 = scn.GetFreepsCard("banner2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(aragorn, knight);
		scn.AttachCardsTo(aragorn, banner);
		// A second copy, on a [gondor] companion who is in no skirmish.
		scn.AttachCardsTo(knight, banner2);
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		assertAttachedTo(banner, aragorn);
		assertAttachedTo(banner2, knight);
		scn.SkipToSite(4);
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		var site3 = scn.GetSite(3);
		scn.ShadowTakeControlOfSite(site1);
		scn.ShadowTakeControlOfSite(site2);
		scn.FreepsTakeControlOfSite(site3);
		assertTrue(scn.ShadowControls(site1));
		assertTrue(scn.ShadowControls(site2));
		assertTrue(scn.FreepsControls(site3));

		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(banner, "liberate"));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(banner2));
		scn.FreepsChooseOptionalTrigger(banner, "liberate");

		assertInDiscard(banner);
		assertAttachedTo(banner2, knight);
		assertFalse(scn.IsSiteControlled(site2));
		assertInZone(Zone.ADVENTURE_PATH, site2);
		assertTrue(scn.ShadowControls(site1));
		assertTrue(scn.FreepsControls(site3));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoSiteAnOpponentControlsTheBannerIsDiscardedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var banner = scn.GetFreepsCard("banner");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, banner);
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		assertAttachedTo(banner, aragorn);
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(banner, "liberate"));
		scn.FreepsChooseOptionalTrigger(banner, "liberate");

		assertInDiscard(banner);
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
