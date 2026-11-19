package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_071_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("commander", "18_71");
					put("beast", "17_40");      // Beast of War: [men] mount, bearer must be a [men] Man
					put("lemenya", "1_232");    // Úlairë Lemenya: a Nazgûl; his text is on companions and allies bearing a [wraith] condition
					put("steed", "1_208");      // Black Steed: a [ringwraith] mount, bearer must be a Nazgûl
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Towers path: site 1 (Eastemnet Gullies) has no terrain, site 2 (Uruk Camp) is a battleground and a plains,
	// site 4 (White Rocks) a battleground and no plains.
	protected VirtualTableScenario GetTowersScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("commander", "18_71");
					put("legion", "11_88");     // Legion of Harad: a [men] Man minion with no text but toil, strength 14
					put("beast", "17_40");      // Beast of War: [men] mount, strength +3, bearer must be a [men] Man; fierce only with a threat
					put("beast2", "17_40");
					put("mouth", "12_73");      // The Mouth of Sauron: a [men] Man minion; his maneuver ability is used only on purpose
					put("msgmount", "12_72");   // Messenger's Mount: [men] mount, strength +3, on The Mouth of Sauron; makes him fierce
					put("soldier", "4_220");    // Desert Soldier: a [raider] Southron Man minion; his trigger is at the start of a skirmish
					put("mumak", "5_73");       // Mûmak: [raider] mount, strength +3, on a Southron; makes him fierce
					put("squad", "11_99");      // Squad of Haradrim: a [men] Man minion; its regroup ability is used only on purpose
					put("poleaxe", "11_95");    // Poleaxe: a [men] hand weapon, strength +2: a possession that is no mount
					put("lemenya", "1_232");    // Úlairë Lemenya: a Nazgûl; his text is on companions and allies bearing a [wraith] condition
					put("steed", "1_208");      // Black Steed: [wraith] mount, strength +2, on a Nazgûl; +2 more only at a plains
					put("rider", "4_265");      // Elite Rider: a [rohan] Man companion; +2 only against a wounded minion
					put("ridersmount", "4_287"); // Rider's Mount: [rohan] mount with no printed bonus, on a [rohan] Man
					put("ridersmount2", "4_287");
					put("hlafwine", "4_281");   // Hlafwine: a [rohan] Man ally; his skirmish ability is used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Man companion, unmounted
					put("legolas", "1_51");     // Legolas, Prince of Mirkwood: an Elf companion; +3 only against a Nazgûl
					put("horse", "4_283");      // Horse of Rohan: [rohan] mount with no printed bonus, on a Man, Elf or Wizard
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.TowersSites);
					put("site2", "4_335");      // Uruk Camp: Battleground. Plains. Exerts each Uruk-hai played; none is
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MumakilCommanderStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Mûmakil Commander, Bold and Grim
		 * Unique: True
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 11
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: While at a battleground, this minion is strength +2.
		 * Each mounted Man is strength +1.
		 * Regroup: Shuffle any number of your [men] mounts in play into your draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("commander");

		assertEquals("Mûmakil Commander", card.getBlueprint().getTitle());
		assertEquals("Bold and Grim", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(11, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void RegroupAbilityIsNotOfferedWithoutAMenMountOfHisInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var commander = scn.GetShadowCard("commander");
		var lemenya = scn.GetShadowCard("lemenya");
		var steed = scn.GetShadowCard("steed");

		scn.StartGame();
		scn.MoveMinionsToTable(commander, lemenya);
		// A mount of his, but not a [men] one.
		scn.AttachCardsTo(lemenya, steed);

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		// Ketura on Q-0112: the ability requires a mount in play, since with none it would only shuffle the deck.
		assertFalse(scn.ShadowActionAvailable(commander));
	}

	@Test
	public void RegroupAbilityShufflesAnyNumberOfHisMenMountsIntoHisDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var commander = scn.GetShadowCard("commander");
		var beast = scn.GetShadowCard("beast");
		var lemenya = scn.GetShadowCard("lemenya");
		var steed = scn.GetShadowCard("steed");

		scn.StartGame();
		scn.MoveMinionsToTable(commander, lemenya);
		scn.AttachCardsTo(commander, beast);
		scn.AttachCardsTo(lemenya, steed);
		assertAttachedTo(beast, commander);
		assertAttachedTo(steed, lemenya);

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(commander));
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		int shadowDeck = scn.GetShadowDeckCount();

		// "Any number": none at all is a choice, and the deck is shuffled all the same (Ketura on Q-0112).
		scn.ShadowUseCardAction(commander);
		assertTrue(scn.ShadowHasCardChoiceAvailable(beast));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(steed));
		assertEquals(0, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCards();

		assertEquals(shadowShuffles + 1, scn.GetShadowDeckShuffleCount());
		assertEquals(freepsShuffles, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertEquals(commander, beast.getAttachedTo());
		assertEquals(lemenya, steed.getAttachedTo());

		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(commander));
		scn.ShadowUseCardAction(commander);
		scn.ShadowChooseCards(beast);

		assertInZone(Zone.DECK, beast);
		assertEquals(shadowDeck + 1, scn.GetShadowDeckCount());
		assertEquals(shadowShuffles + 2, scn.GetShadowDeckShuffleCount());
		assertEquals(freepsShuffles, scn.GetFreepsDeckShuffleCount());
		assertEquals(lemenya, steed.getAttachedTo());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsStrengthPlus2OnceTheFellowshipIsAtABattlegroundAndNoOtherMinionIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var commander = scn.GetShadowCard("commander");
		var legion = scn.GetShadowCard("legion");

		scn.StartGame();
		// Laid early, in the fellowship phase at site 1, to be read before and after the move.
		scn.MoveMinionsToTable(commander, legion);

		assertEquals(1, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertEquals(11, scn.GetStrength(commander));
		assertEquals(14, scn.GetStrength(legion));

		// The fellowship moves to Uruk Camp.
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		// 11 + 2; another minion at the same battleground is not strengthened.
		assertEquals(13, scn.GetStrength(commander));
		assertEquals(14, scn.GetStrength(legion));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsStrengthPlus2AtABattlegroundThatIsNoPlains() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var commander = scn.GetShadowCard("commander");
		var legion = scn.GetShadowCard("legion");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveMinionsToTable(commander, legion);

		// White Rocks.
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		assertEquals(13, scn.GetStrength(commander));
		assertEquals(14, scn.GetStrength(legion));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachMountedManOfEitherSideIsStrengthPlus1AndNoOtherCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var commander = scn.GetShadowCard("commander");
		var legion = scn.GetShadowCard("legion");
		var beast = scn.GetShadowCard("beast");
		var beast2 = scn.GetShadowCard("beast2");
		var soldier = scn.GetShadowCard("soldier");
		var mumak = scn.GetShadowCard("mumak");
		var squad = scn.GetShadowCard("squad");
		var poleaxe = scn.GetShadowCard("poleaxe");
		var lemenya = scn.GetShadowCard("lemenya");
		var steed = scn.GetShadowCard("steed");
		var rider = scn.GetFreepsCard("rider");
		var ridersmount = scn.GetFreepsCard("ridersmount");
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var horse = scn.GetFreepsCard("horse");
		var hlafwine = scn.GetFreepsCard("hlafwine");
		var ridersmount2 = scn.GetFreepsCard("ridersmount2");
		scn.MoveCompanionsToTable(rider, aragorn, legolas);
		scn.MoveAlliesToTable(hlafwine);
		scn.AttachCardsTo(rider, ridersmount);
		scn.AttachCardsTo(hlafwine, ridersmount2);
		scn.AttachCardsTo(legolas, horse);
		assertAttachedTo(ridersmount, rider);
		assertAttachedTo(ridersmount2, hlafwine);
		assertAttachedTo(horse, legolas);

		scn.StartGame();
		// At site 1, which is neither a plains nor a battleground.
		scn.MoveMinionsToTable(legion, soldier, squad, lemenya);
		scn.AttachCardsTo(legion, beast);
		scn.AttachCardsTo(soldier, mumak);
		scn.AttachCardsTo(squad, poleaxe);
		scn.AttachCardsTo(lemenya, steed);
		assertAttachedTo(beast, legion);
		assertAttachedTo(mumak, soldier);
		assertAttachedTo(poleaxe, squad);
		assertAttachedTo(steed, lemenya);

		// Before the Commander is in play.
		assertEquals(6, scn.GetStrength(rider));
		assertEquals(4, scn.GetStrength(hlafwine));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(legolas));
		assertEquals(17, scn.GetStrength(legion));   // 14 + 3
		assertEquals(12, scn.GetStrength(soldier));  // 9 + 3
		assertEquals(11, scn.GetStrength(squad));    // 9 + 2
		assertEquals(11, scn.GetStrength(lemenya));  // 9 + 2

		scn.MoveMinionsToTable(commander);

		// Mounted Men, a companion, an ally and minions of two cultures: +1 each.
		assertEquals(7, scn.GetStrength(rider));
		assertEquals(5, scn.GetStrength(hlafwine));
		assertEquals(18, scn.GetStrength(legion));
		assertEquals(13, scn.GetStrength(soldier));
		// An unmounted Man, a mounted Elf, a Man bearing a possession that is no mount, a mounted Nazgûl: unchanged.
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(legolas));
		assertEquals(11, scn.GetStrength(squad));
		assertEquals(11, scn.GetStrength(lemenya));
		// He bears no mount himself, and site 1 is no battleground.
		assertEquals(11, scn.GetStrength(commander));

		// Once he bears a mount he is a mounted Man too: 11 + 3 + 1.
		scn.AttachCardsTo(commander, beast2);
		assertAttachedTo(beast2, commander);
		assertEquals(15, scn.GetStrength(commander));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void RegroupAbilityOffersOnlyHisMenMountsAndShufflesThoseChosenIntoHisDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var commander = scn.GetShadowCard("commander");
		var legion = scn.GetShadowCard("legion");
		var beast = scn.GetShadowCard("beast");
		var mouth = scn.GetShadowCard("mouth");
		var msgmount = scn.GetShadowCard("msgmount");
		var soldier = scn.GetShadowCard("soldier");
		var mumak = scn.GetShadowCard("mumak");
		var squad = scn.GetShadowCard("squad");
		var poleaxe = scn.GetShadowCard("poleaxe");
		var rider = scn.GetFreepsCard("rider");
		var ridersmount = scn.GetFreepsCard("ridersmount");
		scn.MoveCompanionsToTable(rider);
		scn.AttachCardsTo(rider, ridersmount);
		assertAttachedTo(ridersmount, rider);

		scn.StartGame();
		scn.MoveMinionsToTable(commander, legion, mouth, soldier, squad);
		scn.AttachCardsTo(legion, beast);
		scn.AttachCardsTo(mouth, msgmount);
		scn.AttachCardsTo(soldier, mumak);
		scn.AttachCardsTo(squad, poleaxe);
		assertAttachedTo(beast, legion);
		assertAttachedTo(msgmount, mouth);
		assertAttachedTo(mumak, soldier);
		assertAttachedTo(poleaxe, squad);

		// Not in the maneuver phase, the phase before.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(commander));

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertTrue(scn.ShadowActionAvailable(commander));
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		int shadowDeck = scn.GetShadowDeckCount();
		// 14 + 3 + 1 and 9 + 3 + 1.
		assertEquals(18, scn.GetStrength(legion));
		assertEquals(13, scn.GetStrength(mouth));

		scn.ShadowUseCardAction(commander);
		// His [men] mounts; not his [raider] mount, his possession that is no mount, or the Free Peoples player's mount.
		assertTrue(scn.ShadowHasCardChoiceAvailable(beast, msgmount));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(mumak, poleaxe, ridersmount));
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(beast, msgmount);

		assertInZone(Zone.DECK, beast, msgmount);
		assertEquals(shadowDeck + 2, scn.GetShadowDeckCount());
		assertEquals(shadowShuffles + 1, scn.GetShadowDeckShuffleCount());
		assertEquals(freepsShuffles, scn.GetFreepsDeckShuffleCount());
		// Their bearers are mounted no longer.
		assertEquals(14, scn.GetStrength(legion));
		assertEquals(9, scn.GetStrength(mouth));
		assertAttachedTo(mumak, soldier);
		assertAttachedTo(poleaxe, squad);
		assertAttachedTo(ridersmount, rider);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
