package com.gempukku.lotro.cards.official.set06;

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

public class Card_06_014_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("banner", "6_14");
					put("banner2", "6_14");
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: Elf companion, strength 6, vitality 3; +3 only against a Nazgûl
					put("lorien", "1_53");      // Lórien Elf: Elf companion, strength 4, vitality 2, no other text
					put("celeborn", "1_34");    // Celeborn, Lord of Lórien: an Elf ally; a Fellowship ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no Elf
					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3, Damage +1 only
					// Uruk Seeker: fierce, strength 6, vitality 2; its other text is only against a bearer of a search card
					put("seeker", "4_195");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BannerofElberethStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Banner of Elbereth
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Game Text: Bearer must be an Elf companion.<br>Each time bearer wins a skirmish, you may draw a card.<br><b>Response:</b> If bearer wins a skirmish, discard this possession to liberate a site.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("banner");

		assertEquals("Banner of Elbereth", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void BearerMustBeAnElfCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var banner = scn.GetFreepsCard("banner");
		var arwen = scn.GetFreepsCard("arwen");
		var lorien = scn.GetFreepsCard("lorien");
		var celeborn = scn.GetFreepsCard("celeborn");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(arwen, lorien, aragorn);
		scn.MoveAlliesToTable(celeborn);
		scn.MoveCardsToHand(banner);

		scn.StartGame();

		assertInHand(banner);
		assertTrue(scn.FreepsPlayAvailable(banner));
		scn.FreepsPlayCard(banner);

		// Two Elf companions; not an Elf ally, nor a companion who is no Elf.
		assertTrue(scn.FreepsHasCardChoiceAvailable(arwen, lorien));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(celeborn, aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(lorien);

		assertAttachedTo(banner, lorien);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachTimeBearerWinsASkirmishHeMayDrawACard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var banner = scn.GetFreepsCard("banner");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(arwen);
		scn.AttachCardsTo(arwen, banner);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();

		assertAttachedTo(banner, arwen);
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, savage);
		scn.FreepsResolveSkirmish(arwen);
		int hand = scn.GetFreepsHandCount();
		int deck = scn.GetFreepsDeckCount();

		// 6 against 5.
		scn.PassSkirmishActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(banner, "Draw a card"));
		scn.FreepsChooseOptionalTrigger(banner, "Draw a card");

		assertEquals(hand + 1, scn.GetFreepsHandCount());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertAttachedTo(banner, arwen);
		// The Response to the same win is still on offer.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(banner, "liberate"));
	}

	@Test
	public void DecliningTheDrawDrawsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var banner = scn.GetFreepsCard("banner");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(arwen);
		scn.AttachCardsTo(arwen, banner);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();

		assertAttachedTo(banner, arwen);
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, savage);
		scn.FreepsResolveSkirmish(arwen);
		int hand = scn.GetFreepsHandCount();

		scn.PassSkirmishActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(banner, "Draw a card"));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(hand, scn.GetFreepsHandCount());
		assertAttachedTo(banner, arwen);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NeitherIsOfferedWhenBearerLosesASkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var banner = scn.GetFreepsCard("banner");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(arwen);
		scn.AttachCardsTo(arwen, banner);

		var seeker = scn.GetShadowCard("seeker");

		scn.StartGame();

		assertAttachedTo(banner, arwen);
		scn.MoveMinionsToTable(seeker);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, seeker);
		scn.FreepsResolveSkirmish(arwen);
		int hand = scn.GetFreepsHandCount();

		// 6 against 6: a tie goes to the Shadow side, and Arwen lives through one wound.
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(arwen));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(banner));
		assertEquals(hand, scn.GetFreepsHandCount());
		assertAttachedTo(banner, arwen);
		// The Seeker is fierce: the fierce assignment phase comes next.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void NeitherIsOfferedWhenAnElfCompanionWhoIsNotBearerWins() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var banner = scn.GetFreepsCard("banner");
		var arwen = scn.GetFreepsCard("arwen");
		var lorien = scn.GetFreepsCard("lorien");
		scn.MoveCompanionsToTable(arwen, lorien);
		scn.AttachCardsTo(lorien, banner);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();

		assertAttachedTo(banner, lorien);
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, savage);
		scn.FreepsResolveSkirmish(arwen);
		int hand = scn.GetFreepsHandCount();

		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(savage));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(banner));
		assertEquals(hand, scn.GetFreepsHandCount());
		assertAttachedTo(banner, lorien);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void BearerWinningTheFierceSkirmishAsWellMayDrawAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var banner = scn.GetFreepsCard("banner");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(arwen);
		scn.AttachCardsTo(arwen, banner);

		var seeker = scn.GetShadowCard("seeker");

		scn.StartGame();

		assertAttachedTo(banner, arwen);
		// A strength that only arranges the board: 9 against the Seeker's 6, twice.
		scn.ApplyAdHocModifier(new StrengthModifier(null, arwen, null, 3));
		scn.MoveMinionsToTable(seeker);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, seeker);
		scn.FreepsResolveSkirmish(arwen);
		int hand = scn.GetFreepsHandCount();

		scn.PassSkirmishActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(banner, "Draw a card"));
		scn.FreepsChooseOptionalTrigger(banner, "Draw a card");
		assertEquals(hand + 1, scn.GetFreepsHandCount());
		// The Response is declined, and the Banner stays.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(banner, "liberate"));
		scn.FreepsDeclineOptionalTrigger();
		assertAttachedTo(banner, arwen);
		assertEquals(1, scn.GetWoundsOn(seeker));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(arwen, seeker);
		scn.FreepsResolveSkirmish(arwen);
		scn.PassFierceSkirmishActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(banner, "Draw a card"));
		scn.FreepsChooseOptionalTrigger(banner, "Draw a card");
		assertEquals(hand + 2, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(banner, "liberate"));
	}

	@Test
	public void ResponseDiscardsTheBannerToLiberateTheHighestSiteAnOpponentControls() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var banner = scn.GetFreepsCard("banner");
		var banner2 = scn.GetFreepsCard("banner2");
		var arwen = scn.GetFreepsCard("arwen");
		var lorien = scn.GetFreepsCard("lorien");
		scn.MoveCompanionsToTable(arwen, lorien);
		scn.AttachCardsTo(arwen, banner);
		// A second copy, on an Elf who is in no skirmish.
		scn.AttachCardsTo(lorien, banner2);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		assertAttachedTo(banner, arwen);
		assertAttachedTo(banner2, lorien);
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
		scn.FreepsAssignToMinions(arwen, savage);
		scn.FreepsResolveSkirmish(arwen);
		scn.PassSkirmishActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(banner, "liberate"));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(banner2));
		scn.FreepsChooseOptionalTrigger(banner, "liberate");

		assertInDiscard(banner);
		assertAttachedTo(banner2, lorien);
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
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(arwen);
		scn.AttachCardsTo(arwen, banner);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();

		assertAttachedTo(banner, arwen);
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, savage);
		scn.FreepsResolveSkirmish(arwen);
		scn.PassSkirmishActions();

		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(banner, "liberate"));
		scn.FreepsChooseOptionalTrigger(banner, "liberate");

		assertInDiscard(banner);
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
